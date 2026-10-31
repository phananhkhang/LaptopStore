'use strict';

// Offline approximation of the small Figma Plugin API surface used by this
// project's authoring scripts. This is a layout/preview aid, not a .fig writer.
const copy = value => value === undefined ? undefined : JSON.parse(JSON.stringify(value));
const finite = (value, fallback = 0) => Number.isFinite(Number(value)) ? Number(value) : fallback;
const plainColor = value => value && typeof value === 'object' && ['r','g','b'].every(k => k in value);

function textMeasure(node, width) {
  const size = finite(node.fontSize, 14);
  const height = node.lineHeight?.unit === 'PIXELS' ? node.lineHeight.value : size * finite(node.lineHeight?.value, 150) / 100;
  const letterSpacing = node.letterSpacing?.unit === 'PIXELS' ? finite(node.letterSpacing.value) : 0;
  const weight = /bold/i.test(node.fontName?.style || '') ? 1.045 : 1;
  const charWidth = char => (/\s/.test(char) ? .29 : /[ilItfj.,:;'!|]/.test(char) ? .27 : /[MW@%]/.test(char) ? .86 : /[A-Z0-9]/.test(char) ? .61 : /[\u2e80-\uffff]/.test(char) ? .9 : .53) * size * weight + letterSpacing;
  const naturalLines = String(node.characters || '').split('\n');
  const naturalWidth = Math.max(1, ...naturalLines.map(line => [...line].reduce((sum, char) => sum + charWidth(char), 0)));
  const constrained = width > 0 && node.textAutoResize !== 'WIDTH_AND_HEIGHT';
  let lineCount = 0;
  for (const line of naturalLines) {
    if (!constrained) { lineCount++; continue; }
    let used = 0; lineCount++;
    for (const word of line.split(/(\s+)/)) {
      const wordWidth = [...word].reduce((sum, char) => sum + charWidth(char), 0);
      if (used > 0 && used + wordWidth > width) { lineCount++; used = 0; }
      if (wordWidth > width) { const extra = Math.max(0, Math.ceil(wordWidth / width) - 1); lineCount += extra; used = wordWidth - extra * width; }
      else used += wordWidth;
    }
  }
  return { width: constrained ? width : Math.ceil(naturalWidth), height: Math.ceil(Math.max(1, lineCount) * height) };
}

function sizing(node, axis) {
  const explicit = axis === 'width' ? node.layoutSizingHorizontal : node.layoutSizingVertical;
  if (explicit === 'FILL' || explicit === 'HUG') return explicit;
  const main = node.layoutMode === 'HORIZONTAL' ? 'width' : 'height';
  if (node.layoutMode !== 'NONE' && ((axis === main && node.primaryAxisSizingMode === 'AUTO') || (axis !== main && node.counterAxisSizingMode === 'AUTO'))) return 'HUG';
  return 'FIXED';
}

function measure(node, widthConstraint) {
  if (!node || node.visible === false) return { width: 0, height: 0 };
  if (node.type === 'TEXT') {
    const limit = widthConstraint || (node.textAutoResize !== 'WIDTH_AND_HEIGHT' ? node._width : undefined);
    const text = textMeasure(node, limit);
    return { width: node.textAutoResize === 'NONE' ? node._width || text.width : text.width, height: node.textAutoResize === 'NONE' ? node._height || text.height : text.height };
  }
  if (node.originalSvg) return { width: node._width || 100, height: node._height || 100 };
  const children = node.children.filter(c => c.visible !== false && c.layoutPositioning !== 'ABSOLUTE');
  const padW = finite(node.paddingLeft) + finite(node.paddingRight);
  const padH = finite(node.paddingTop) + finite(node.paddingBottom);
  let width = node._width || 100, height = node._height || 100;
  if (node.layoutMode === 'HORIZONTAL' || node.layoutMode === 'VERTICAL') {
    const horizontal = node.layoutMode === 'HORIZONTAL';
    const dims = children.map(c => measure(c));
    const sumW = dims.reduce((v,d) => v + d.width, 0), sumH = dims.reduce((v,d) => v + d.height, 0);
    const maxW = Math.max(0, ...dims.map(d => d.width)), maxH = Math.max(0, ...dims.map(d => d.height));
    const spacing = finite(node.itemSpacing) * Math.max(0, children.length - 1);
    if (sizing(node, 'width') === 'HUG') width = padW + (horizontal ? sumW + spacing : maxW);
    if (sizing(node, 'height') === 'HUG') height = padH + (horizontal ? maxH : sumH + spacing);
    if (node.layoutWrap === 'WRAP' && horizontal && sizing(node, 'width') !== 'HUG') {
      let rowWidth = 0, rowHeight = 0, totalHeight = 0;
      const contentWidth = Math.max(1,width-padW);
      for (const d of dims) {
        if (rowWidth && rowWidth + finite(node.itemSpacing) + d.width > contentWidth) { totalHeight += rowHeight + finite(node.counterAxisSpacing,node.itemSpacing); rowWidth = 0; rowHeight = 0; }
        rowWidth += (rowWidth ? finite(node.itemSpacing) : 0) + d.width; rowHeight = Math.max(rowHeight,d.height);
      }
      if (sizing(node,'height') === 'HUG') height = padH + totalHeight + rowHeight;
    }
  } else if (['PAGE','DOCUMENT','GROUP','COMPONENT_SET'].includes(node.type)) {
    if (!node._resized || node.type === 'PAGE' || node.type === 'DOCUMENT') {
      width = Math.max(0, ...children.map(c => finite(c.x) + measure(c).width));
      height = Math.max(0, ...children.map(c => finite(c.y) + measure(c).height));
    }
  }
  return { width: Math.max(0,width), height: Math.max(0,height) };
}

function computeLayout(node, forcedWidth, forcedHeight, localX, localY) {
  const intrinsic = measure(node, forcedWidth);
  const width = Math.max(0,forcedWidth ?? intrinsic.width), height = Math.max(0,forcedHeight ?? intrinsic.height);
  node._computed = { x: localX ?? node.x ?? 0, y: localY ?? node.y ?? 0, width, height };
  if (!node.children.length || node.originalSvg) return node._computed;
  if (!['HORIZONTAL','VERTICAL'].includes(node.layoutMode)) {
    for (const child of node.children) computeLayout(child,undefined,undefined,child.x,child.y);
    return node._computed;
  }
  const horizontal = node.layoutMode === 'HORIZONTAL';
  const visible = node.children.filter(c => c.visible !== false && c.layoutPositioning !== 'ABSOLUTE');
  const padL = finite(node.paddingLeft), padT = finite(node.paddingTop), padR = finite(node.paddingRight), padB = finite(node.paddingBottom);
  const contentW = Math.max(0,width-padL-padR), contentH = Math.max(0,height-padT-padB);
  const gap = finite(node.itemSpacing);
  const measured = visible.map(c => measure(c));
  if (horizontal && node.layoutWrap === 'WRAP') {
    let x = padL, y = padT, lineH = 0;
    visible.forEach((child,i) => {
      const d = measured[i];
      if (x > padL && x + d.width > width-padR) { x=padL; y+=lineH+finite(node.counterAxisSpacing,gap); lineH=0; }
      computeLayout(child,d.width,d.height,x,y); x+=d.width+gap; lineH=Math.max(lineH,d.height);
    });
  } else {
    const mainKey = horizontal ? 'width' : 'height', crossKey = horizontal ? 'height' : 'width';
    const mainAvailable = horizontal ? contentW : contentH, crossAvailable = horizontal ? contentH : contentW;
    const fillCount = visible.filter(c => sizing(c,mainKey)==='FILL').length;
    const fixedTotal = measured.reduce((sum,d,i) => sum + (sizing(visible[i],mainKey)==='FILL' ? 0 : d[mainKey]),0);
    const fillSize = fillCount ? Math.max(0,(mainAvailable-fixedTotal-gap*Math.max(0,visible.length-1))/fillCount) : 0;
    const sizes = visible.map((c,i) => {
      let childW = measured[i].width, childH=measured[i].height;
      if (sizing(c,mainKey)==='FILL') { if(horizontal)childW=fillSize;else childH=fillSize; }
      if (sizing(c,crossKey)==='FILL' || c.layoutAlign==='STRETCH') { if(horizontal)childH=crossAvailable;else childW=crossAvailable; }
      if (c.type==='TEXT' && c.textAutoResize!=='NONE' && (sizing(c,'width')==='FILL' || c.textAutoResize==='HEIGHT')) childH=textMeasure(c,childW).height;
      return {width:childW,height:childH};
    });
    const used = sizes.reduce((sum,d)=>sum+d[mainKey],0) + gap*Math.max(0,visible.length-1);
    let cursor = horizontal ? padL : padT, actualGap=gap;
    if (node.primaryAxisAlignItems==='CENTER') cursor+=Math.max(0,(mainAvailable-used)/2);
    if (node.primaryAxisAlignItems==='MAX') cursor+=Math.max(0,mainAvailable-used);
    if (node.primaryAxisAlignItems==='SPACE_BETWEEN' && visible.length>1) actualGap=Math.max(gap,(mainAvailable-sizes.reduce((sum,d)=>sum+d[mainKey],0))/(visible.length-1));
    visible.forEach((child,i) => {
      const d=sizes[i]; let cross=horizontal?padT:padL;
      if (node.counterAxisAlignItems==='CENTER') cross+=Math.max(0,(crossAvailable-d[crossKey])/2);
      if (node.counterAxisAlignItems==='MAX') cross+=Math.max(0,crossAvailable-d[crossKey]);
      computeLayout(child,d.width,d.height,horizontal?cursor:cross,horizontal?cross:cursor);
      cursor+=d[mainKey]+actualGap;
    });
  }
  for(const c of node.children.filter(c=>c.layoutPositioning==='ABSOLUTE'))computeLayout(c,undefined,undefined,c.x,c.y);
  return node._computed;
}

class MockNode {
  constructor(figma,type) {
    Object.defineProperty(this,'_figma',{value:figma,writable:true,enumerable:false});
    this.id=figma._newId(type.toLowerCase()); this.type=type; this.name=type; this.parent=null; this.children=[];
    this.x=0;this.y=0;this._width=100;this._height=100;this._resized=false;this.visible=true;this.opacity=1;
    this.fills=[];this.strokes=[];this.effects=[];this.strokeWeight=0;this.cornerRadius=0;this.clipsContent=false;
    this.layoutMode='NONE';this.layoutSizingHorizontal='FIXED';this.layoutSizingVertical='FIXED';
    this.primaryAxisSizingMode='FIXED';this.counterAxisSizingMode='FIXED';this.primaryAxisAlignItems='MIN';this.counterAxisAlignItems='MIN';
    this.itemSpacing=0;this.counterAxisSpacing=0;this.paddingTop=0;this.paddingBottom=0;this.paddingLeft=0;this.paddingRight=0;
    this.boundVariables={};this.reactions=[];this.componentPropertyReferences={};this.componentPropertyDefinitions={};this.componentProperties={};
    if(type==='TEXT'){this._characters='';this.fontName={family:'Be Vietnam Pro',style:'Regular'};this.fontSize=14;this.lineHeight={unit:'PERCENT',value:150};this.textAutoResize='WIDTH_AND_HEIGHT';this.textAlignHorizontal='LEFT';this.textAlignVertical='TOP';this._height=21;this._width=1;}
    figma._nodes.set(this.id,this);
  }
  get width(){return this._computed?.width ?? measure(this).width;}
  get height(){return this._computed?.height ?? measure(this).height;}
  get characters(){return this._characters||'';}
  set characters(value){this._characters=String(value);this._computed=undefined;}
  get absoluteBoundingBox(){let x=this._computed?.x??this.x,y=this._computed?.y??this.y;for(let p=this.parent;p;p=p.parent){x+=p._computed?.x??p.x??0;y+=p._computed?.y??p.y??0;}return{x,y,width:this.width,height:this.height};}
  get absoluteRenderBounds(){return this.absoluteBoundingBox;}
  get absoluteTransform(){const b=this.absoluteBoundingBox;return[[1,0,b.x],[0,1,b.y]];}
  get relativeTransform(){return[[1,0,this.x],[0,1,this.y]];}
  set relativeTransform(v){this.x=v?.[0]?.[2]||0;this.y=v?.[1]?.[2]||0;}
  get inferredAutoLayout(){return null;}
  get removed(){return !!this._removed;}
  appendChild(child){if(child.parent){const i=child.parent.children.indexOf(child);if(i>=0)child.parent.children.splice(i,1);}child.parent=this;this.children.push(child);this._computed=undefined;return child;}
  insertChild(index,child){this.appendChild(child);this.children.splice(this.children.length-1,1);this.children.splice(index,0,child);return child;}
  insertChildren(index,children){for(let i=0;i<children.length;i++)this.insertChild(index+i,children[i]);}
  resize(width,height){const oldW=this._width||100,oldH=this._height||100;const nextW=Math.max(0.01,finite(width,100)),nextH=Math.max(0.01,finite(height,100));if(this.layoutMode==='NONE'){for(const child of this.children){const sx=nextW/oldW,sy=nextH/oldH;if(child.constraints?.horizontal==='SCALE'){child.x*=sx;child._width*=sx;}if(child.constraints?.vertical==='SCALE'){child.y*=sy;child._height*=sy;}child._computed=undefined;}}this._width=nextW;this._height=nextH;this._resized=true;this._computed=undefined;return this;}
  resizeWithoutConstraints(width,height){return this.resize(width,height);}
  rescale(scale){scale=finite(scale,1);this._width*=scale;this._height*=scale;this._resized=true;this._computed=undefined;for(const key of ['fontSize','strokeWeight','cornerRadius','topLeftRadius','topRightRadius','bottomLeftRadius','bottomRightRadius','itemSpacing','counterAxisSpacing','paddingTop','paddingBottom','paddingLeft','paddingRight','minWidth','minHeight','maxWidth','maxHeight'])if(Number.isFinite(this[key]))this[key]*=scale;if(this.lineHeight?.unit==='PIXELS')this.lineHeight={...this.lineHeight,value:this.lineHeight.value*scale};if(this.letterSpacing?.unit==='PIXELS')this.letterSpacing={...this.letterSpacing,value:this.letterSpacing.value*scale};for(const c of this.children){c.x*=scale;c.y*=scale;c.rescale(scale);}return this;}
  remove(){if(this.parent){const i=this.parent.children.indexOf(this);if(i>=0)this.parent.children.splice(i,1);this.parent=null;}this._removed=true;this._figma._nodes.delete(this.id);}
  findAll(predicate=()=>true){const out=[];const visit=n=>{for(const child of n.children){if(predicate(child))out.push(child);visit(child);}};visit(this);return out;}
  findOne(predicate){for(const child of this.children){if(predicate(child))return child;const found=child.findOne(predicate);if(found)return found;}return null;}
  findChildren(predicate){return this.children.filter(predicate);}
  findAllWithCriteria(criteria={}){return this.findAll(n=>(!criteria.types||criteria.types.includes(n.type))&&(!criteria.pluginData||criteria.pluginData.every(k=>n.getPluginData(k.key)===k.value)));}
  query(selector){if(typeof selector==='function')return this.findAll(selector);if(typeof selector==='object')return this.findAllWithCriteria(selector);const match=String(selector||'').match(/\[name=["']?([^"'\]]+)/);return this.findAll(n=>match?n.name===match[1]:n.type===String(selector).toUpperCase()||n.name===selector);}
  setBoundVariable(field,variable){this.boundVariables[field]={type:'VARIABLE_ALIAS',id:typeof variable==='string'?variable:variable.id};}
  setExplicitVariableModeForCollection(collection,mode){this.explicitVariableModes={...this.explicitVariableModes,[typeof collection==='string'?collection:collection.id]:mode};}
  clearExplicitVariableModeForCollection(collection){delete this.explicitVariableModes?.[typeof collection==='string'?collection:collection.id];}
  addComponentProperty(name,type,defaultValue,options){const key=name+'#'+this._figma._newId('prop');this.componentPropertyDefinitions[key]={type,defaultValue,...copy(options||{})};return key;}
  editComponentProperty(name,options){Object.assign(this.componentPropertyDefinitions[name],copy(options));return name;}
  deleteComponentProperty(name){delete this.componentPropertyDefinitions[name];}
  setProperties(values){for(const[key,value]of Object.entries(values||{})){const definition=this.componentPropertyDefinitions[key]||this.mainComponent?.componentPropertyDefinitions[key];const actual=key in this.componentProperties?key:Object.keys(this.componentProperties).find(k=>k.split('#')[0]===key)||key;this.componentProperties[actual]={type:definition?.type||this.componentProperties[actual]?.type||'TEXT',value};const all=[this,...this.findAll()];for(const n of all){for(const[field,ref]of Object.entries(n.componentPropertyReferences||{})){if(ref===actual||ref===key){if(field==='characters')n.characters=value;else n[field]=value;}}}}return this;}
  createInstance(){const result=this._deepClone('INSTANCE');result.mainComponent=this;result.name=this.name;result.componentPropertyDefinitions=copy(this.componentPropertyDefinitions);result.componentProperties={};for(const[k,d]of Object.entries(this.componentPropertyDefinitions))result.componentProperties[k]={type:d.type,value:copy(d.defaultValue)};result.setProperties(Object.fromEntries(Object.entries(result.componentProperties).map(([k,v])=>[k,v.value])));this._figma.currentPage.appendChild(result);return result;}
  swapComponent(component){const old=Object.fromEntries(Object.entries(this.componentProperties||{}).map(([k,v])=>[k.split('#')[0],v.value]));for(const c of [...this.children])c.remove();for(const c of component.children)this.appendChild(c._deepClone());this.mainComponent=component;this.componentPropertyDefinitions=copy(component.componentPropertyDefinitions);this.componentProperties={};for(const[k,d]of Object.entries(component.componentPropertyDefinitions))this.componentProperties[k]={type:d.type,value:old[k.split('#')[0]]??copy(d.defaultValue)};this.setProperties(Object.fromEntries(Object.entries(this.componentProperties).map(([k,v])=>[k,v.value])));return this;}
  _deepClone(typeOverride){const clone=new MockNode(this._figma,typeOverride||this.type);for(const key of Object.keys(this)){if(['id','type','parent','children','_computed','_figma','mainComponent'].includes(key))continue;clone[key]=copy(this[key]);}if(this.mainComponent)clone.mainComponent=this.mainComponent;for(const c of this.children)clone.appendChild(c._deepClone());return clone;}
  clone(){const clone=this._deepClone();(this.parent||this._figma.currentPage).appendChild(clone);return clone;}
  detachInstance(){this.type='FRAME';delete this.mainComponent;return this;}
  async getMainComponentAsync(){return this.mainComponent||null;}
  async setReactionsAsync(reactions){this.reactions=copy(reactions);}
  setPluginData(key,value){this._pluginData={...this._pluginData,[key]:String(value)};}
  getPluginData(key){return this._pluginData?.[key]||'';}
  getPluginDataKeys(){return Object.keys(this._pluginData||{});}
  async setTextStyleIdAsync(id){this.textStyleId=id;const s=this._figma._styles.get(id);if(s)for(const k of ['fontName','fontSize','lineHeight','letterSpacing','textCase','textDecoration'])if(s[k]!==undefined)this[k]=copy(s[k]);}
  getRangeFontName(){return this.fontName;}
  getRangeFontSize(){return this.fontSize;}
  getRangeFills(){return this.fills;}
  setRangeFontName(start,end,value){this.fontName=value;}
  setRangeFontSize(start,end,value){this.fontSize=value;}
  setRangeFills(start,end,value){this.fills=copy(value);}
  setRangeTextStyleId(start,end,id){this.textStyleId=id;}
  async exportAsync(){return new Uint8Array();}
}

function createMockFigma(){
  let next=0;
  const figma={_nodes:new Map(),_vars:new Map(),_collections:new Map(),_styles:new Map(),_newId:prefix=>prefix+':'+(++next),mixed:Symbol('figma.mixed'),apiVersion:'1.0.0',editorType:'figma',command:'offline-preview',skipInvisibleInstanceChildren:false};
  figma.root=new MockNode(figma,'DOCUMENT');figma.root.name='LaptopStore';
  figma.currentPage=new MockNode(figma,'PAGE');figma.currentPage.name='Page 1';figma.currentPage.selection=[];figma.currentPage.flowStartingPoints=[];figma.root.appendChild(figma.currentPage);
  const create=type=>{const n=new MockNode(figma,type);figma.currentPage.appendChild(n);return n;};
  for(const[type,name]of [['FRAME','createFrame'],['GROUP','createGroup'],['COMPONENT','createComponent'],['TEXT','createText'],['RECTANGLE','createRectangle'],['ELLIPSE','createEllipse'],['VECTOR','createVector'],['LINE','createLine'],['POLYGON','createPolygon'],['STAR','createStar'],['SECTION','createSection']])figma[name]=()=>create(type);
  figma.createPage=()=>{const p=new MockNode(figma,'PAGE');p.selection=[];p.flowStartingPoints=[];figma.root.appendChild(p);return p;};
  figma.createAutoLayout=direction=>{const n=create('FRAME');n.layoutMode=direction||'VERTICAL';n.layoutSizingHorizontal='HUG';n.layoutSizingVertical='HUG';return n;};
  figma.createNodeFromSvg=svg=>{const n=create('FRAME');n.name='SVG';n.originalSvg=String(svg);const view=String(svg).match(/viewBox=["']([\d.\s,-]+)["']/i)?.[1]?.split(/[\s,]+/).map(Number);const width=Number(String(svg).match(/<svg[^>]*\bwidth=["']([\d.]+)/i)?.[1])||view?.[2]||100;const height=Number(String(svg).match(/<svg[^>]*\bheight=["']([\d.]+)/i)?.[1])||view?.[3]||100;n.resize(width,height);return n;};
  figma.combineAsVariants=(nodes,parent)=>{const set=create('COMPONENT_SET');set.name='Component set';(parent||figma.currentPage).appendChild(set);for(const n of nodes)set.appendChild(n);return set;};
  figma.group=(nodes,parent,index)=>{const g=create('GROUP');(parent||figma.currentPage).appendChild(g);for(const n of nodes)g.appendChild(n);if(index!==undefined)parent.insertChild(index,g);return g;};
  figma.getNodeById=id=>figma._nodes.get(id)||null;figma.getNodeByIdAsync=async id=>figma.getNodeById(id);
  figma.setCurrentPageAsync=async page=>{figma.currentPage=page;};
  figma.loadFontAsync=async()=>{};figma.listAvailableFontsAsync=async()=>['Regular','Medium','SemiBold','Bold'].map(style=>({fontName:{family:'Be Vietnam Pro',style}}));
  figma.loadAllPagesAsync=async()=>{};figma.notify=()=>({cancel(){}});figma.closePlugin=()=>{};figma.commitUndo=()=>{};figma.triggerUndo=()=>{};
  figma.viewport={center:{x:0,y:0},zoom:1,bounds:{x:0,y:0,width:1440,height:1080},scrollAndZoomIntoView(){}};
  figma.ui={postMessage(){},show(){},hide(){}};figma.showUI=()=>{};figma.clientStorage={async getAsync(){},async setAsync(){},async deleteAsync(){}};
  const createStyle=type=>{const s={id:figma._newId('style'),type,name:'',description:'',remove(){figma._styles.delete(this.id);}};figma._styles.set(s.id,s);return s;};
  figma.createTextStyle=()=>createStyle('TEXT');figma.createPaintStyle=()=>createStyle('PAINT');figma.createEffectStyle=()=>createStyle('EFFECT');figma.createGridStyle=()=>createStyle('GRID');
  for(const[type,name]of [['TEXT','Text'],['PAINT','Paint'],['EFFECT','Effect'],['GRID','Grid']]){figma['getLocal'+name+'Styles']=()=>[...figma._styles.values()].filter(s=>s.type===type);figma['getLocal'+name+'StylesAsync']=async()=>figma['getLocal'+name+'Styles']();}
  figma.getStyleById=id=>figma._styles.get(id)||null;figma.getStyleByIdAsync=async id=>figma.getStyleById(id);
  figma.variables={
    createVariableCollection(name){const modeId=figma._newId('mode');const c={id:figma._newId('collection'),name,defaultModeId:modeId,modes:[{modeId,name:'Mode 1'}],variableIds:[],hiddenFromPublishing:false,remote:false,renameMode(id,n){const m=this.modes.find(m=>m.modeId===id);if(m)m.name=n;},addMode(name){const id=figma._newId('mode');this.modes.push({modeId:id,name});return id;},removeMode(id){this.modes=this.modes.filter(m=>m.modeId!==id);},remove(){figma._collections.delete(this.id);}};figma._collections.set(c.id,c);return c;},
    createVariable(name,collection,type){const c=typeof collection==='string'?figma._collections.get(collection):collection;const v={id:figma._newId('variable'),name,variableCollectionId:c.id,resolvedType:type,valuesByMode:{},scopes:[],codeSyntax:{},remote:false,hiddenFromPublishing:false,description:'',setValueForMode(mode,value){this.valuesByMode[mode]=copy(value);},setVariableCodeSyntax(platform,syntax){this.codeSyntax[platform]=syntax;},resolveForConsumer(){return{value:resolveVariable(figma,this.id),resolvedType:this.resolvedType};},remove(){figma._vars.delete(this.id);}};c.variableIds.push(v.id);figma._vars.set(v.id,v);return v;},
    getLocalVariables:()=>[...figma._vars.values()],getLocalVariablesAsync:async()=>[...figma._vars.values()],
    getLocalVariableCollections:()=>[...figma._collections.values()],getLocalVariableCollectionsAsync:async()=>[...figma._collections.values()],
    getVariableById:id=>figma._vars.get(id)||null,getVariableByIdAsync:async id=>figma._vars.get(id)||null,
    getVariableCollectionById:id=>figma._collections.get(id)||null,getVariableCollectionByIdAsync:async id=>figma._collections.get(id)||null,
    createVariableAlias:v=>({type:'VARIABLE_ALIAS',id:typeof v==='string'?v:v.id}),
    setBoundVariableForPaint(paint,field,variable){return{...copy(paint),boundVariables:{...copy(paint.boundVariables||{}),[field]:{type:'VARIABLE_ALIAS',id:typeof variable==='string'?variable:variable.id}}};},
    setBoundVariableForEffect(effect,field,variable){return{...copy(effect),boundVariables:{...copy(effect.boundVariables||{}),[field]:{type:'VARIABLE_ALIAS',id:typeof variable==='string'?variable:variable.id}}};}
  };
  figma._computeLayout=(node=figma.currentPage)=>computeLayout(node);
  figma._serialize=node=>serializeNode(node||figma.currentPage);
  return figma;
}

function resolveVariable(figma,id,seen=new Set()){
  if(!id||seen.has(id))return undefined;seen.add(id);
  const variable=figma._vars.get(id);if(!variable)return undefined;
  const collection=figma._collections.get(variable.variableCollectionId);
  const value=variable.valuesByMode[collection?.defaultModeId]??Object.values(variable.valuesByMode)[0];
  return value?.type==='VARIABLE_ALIAS'?resolveVariable(figma,value.id,seen):copy(value);
}

function resolvePaint(paint,figma){
  const p=copy(paint);if(!p)return p;
  for(const[field,alias]of Object.entries(p.boundVariables||{})){const value=resolveVariable(figma,alias.id);if(value!==undefined)p[field]=value;}
  if(p.color?.type==='VARIABLE_ALIAS')p.color=resolveVariable(figma,p.color.id);
  return p;
}

function serializeNode(node){
  if(!node)return null;
  computeLayout(node);
  const figma=node._figma;
  const visit=n=>{
    const result={};
    for(const[key,value]of Object.entries(n)){
      if(key.startsWith('_')||['parent','children','mainComponent'].includes(key)||typeof value==='function'||typeof value==='symbol')continue;
      try{result[key]=copy(value);}catch{}
    }
    Object.assign(result,n._computed||{x:n.x,y:n.y,width:n.width,height:n.height});
    if(n.type==='TEXT'){result.characters=n.characters;const style=figma._styles.get(n.textStyleId);if(style)result.textStyleName=style.name;}
    if(n.originalSvg){result.originalSvg=n.originalSvg;result.svg=n.originalSvg;}
    if(n.mainComponent){result.mainComponentId=n.mainComponent.id;result.mainComponentName=n.mainComponent.name;}
    result.fills=(n.fills||[]).map(p=>resolvePaint(p,figma));result.strokes=(n.strokes||[]).map(p=>resolvePaint(p,figma));
    for(const[field,alias]of Object.entries(n.boundVariables||{})){const value=resolveVariable(figma,alias.id);if(value!==undefined&&typeof value!=='object')result[field]=value;}
    result.children=n.children.filter(c=>!c.removed).map(visit);
    return result;
  };
  return visit(node);
}

module.exports={createMockFigma,serializeNode,computeLayout,measure,textMeasure,resolveVariable,MockNode};
