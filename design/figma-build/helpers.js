const ids = [];
const made = n => { ids.push(n.id); return n; };
const rgb = h => ({r:parseInt(h.slice(1,3),16)/255,g:parseInt(h.slice(3,5),16)/255,b:parseInt(h.slice(5,7),16)/255});
const palette = {orange:'#E65319',orangeDark:'#BD3D0D',orangeSoft:'#FFF1E8',orangePale:'#FFF8F3',ink:'#212226',muted:'#74777F',line:'#E9EAED',white:'#FFFFFF',canvas:'#F7F8FA',green:'#16835B',greenSoft:'#EAF7F0',blue:'#3467B1',blueSoft:'#EDF4FF',red:'#BE3E3E',redSoft:'#FFF0F0',dark:'#292C32'};
await Promise.all(['Regular','Medium','SemiBold','Bold'].map(style=>figma.loadFontAsync({family:'Be Vietnam Pro',style})));
const vars = Object.fromEntries((await figma.variables.getLocalVariablesAsync()).map(v=>[v.name,v]));
const styles = Object.fromEntries((await figma.getLocalTextStylesAsync()).map(s=>[s.name,s]));
function paint(k){const p={type:'SOLID',color:rgb(palette[k]||k)};return vars['color/'+k]?figma.variables.setBoundVariableForPaint(p,'color',vars['color/'+k]):p;}
function fill(n,k){n.fills=k?[paint(k)]:[];return n;}
function radius(n,v=12){n.cornerRadius=v;const t=vars['radius/'+v];if(t)for(const f of ['topLeftRadius','topRightRadius','bottomLeftRadius','bottomRightRadius'])n.setBoundVariable(f,t);return n;}
function gap(n,v){n.itemSpacing=v;if(vars['space/'+v])n.setBoundVariable('itemSpacing',vars['space/'+v]);}
function pad(n,v){for(const k of ['paddingTop','paddingBottom','paddingLeft','paddingRight']){n[k]=v;if(vars['space/'+v])n.setBoundVariable(k,vars['space/'+v]);}}
function border(n,k='line'){n.strokes=[paint(k)];n.strokeWeight=1;return n;}
function auto(p,name,dir='VERTICAL',w,h,bg,g=16,padding=0){const n=made(figma.createAutoLayout(dir));n.name=name;fill(n,bg);gap(n,g);pad(n,padding);if(w||h)n.resize(w||10,h||10);p.appendChild(n);n.layoutSizingHorizontal=w?'FIXED':'HUG';n.layoutSizingVertical=h?'FIXED':'HUG';return n;}
function text(p,s,size=14,weight='Regular',color='ink',w){const n=made(figma.createText());n.name=s.slice(0,50);n.fontName={family:'Be Vietnam Pro',style:weight};n.fontSize=size;n.lineHeight={unit:'PERCENT',value:150};const st=styles['LaptopStore/'+size+'/'+weight];if(st)n.textStyleId=st.id;n.characters=s;n.fills=[paint(color)];p.appendChild(n);if(w){n.textAutoResize='HEIGHT';n.resize(w,n.height);}return n;}
function grow(n){n.layoutSizingHorizontal='FILL';return n;}
function spacer(p){const n=auto(p,'Flexible space','HORIZONTAL',1,1,null,0);grow(n);return n;}
function line(p,w){return auto(p,'Divider','HORIZONTAL',w,1,'line',0);}
function svg(p,s,name,w,h){const n=made(figma.createNodeFromSvg(s));n.name=name;p.appendChild(n);if(w)n.resize(w,h||w);for(const q of n.findAll(()=>true))ids.push(q.id);return n;}
function icon(p,key,color='muted',size=22){return svg(p,('<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="'+(palette[color]||color)+'" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">'+(ICONS[key]||ICONS.box)+'</svg>').replace(/currentColor/g,palette[color]||color),'Icon/'+key,size,size);}
function component(name,w,h,dir='VERTICAL',bg='white',g=16,padding=0){const n=made(figma.createComponent());n.name=name;n.resize(w,h);n.layoutMode=dir;n.primaryAxisSizingMode='FIXED';n.counterAxisSizingMode='FIXED';gap(n,g);pad(n,padding);fill(n,bg);return n;}
function prop(c,n,key,value){const k=c.addComponentProperty(key,'TEXT',value);n.componentPropertyReferences={characters:k};return k;}
function props(i,obj){const out={};for(const [k,v]of Object.entries(obj)){const pk=Object.keys(i.componentProperties).find(x=>x.split('#')[0]===k);if(pk)out[pk]=String(v);}i.setProperties(out);return i;}
function instance(p,key,obj={},w){const c=COMPS[key];if(!c)throw new Error('Missing component '+key);const n=made(c.createInstance());p.appendChild(n);props(n,obj);if(w)n.resize(w,n.height);return n;}
function btn(p,label,type='Primary',w){const n=instance(p,'Button/'+type,{Label:label},w);n.name='Action/'+label;return n;}
function badge(p,label,tone='Orange'){return instance(p,'Badge/'+tone,{Label:label});}
function field(p,label,value,w=340){const n=instance(p,'Input',{Label:label,Value:value},w);const c=n.children.find(x=>x.name==='Input control');if(c)c.resize(w,48);return n;}
function metric(p,title,value,delta,kind='chart',w=250){const n=instance(p,'Metric',{Title:title,Value:value,Delta:delta},w);n.name='Metric/'+title;return n;}
function sectionTitle(p,title,sub,action){const r=auto(p,'Section heading','HORIZONTAL',null,null,null,16);r.layoutSizingHorizontal='FILL';r.counterAxisAlignItems='CENTER';const c=auto(r,'Heading','VERTICAL',null,null,null,4);text(c,title,22,'Bold');if(sub)text(c,sub,13,'Regular','muted');spacer(r);if(action)btn(r,action,'Ghost');return r;}
function panel(p,name,w,h,padding=24){const n=auto(p,name,'VERTICAL',w,h,'white',16,padding);radius(n,16);border(n);return n;}
function screen(key){return figma.getNodeByIdAsync(S.screens[key]);}
function shop(p,active='Laptop'){const h=instance(p,'Shop header');return h;}
function pageContent(p,w=1280,g=24){const wrap=auto(p,'Page content','VERTICAL',w,null,null,g);wrap.counterAxisAlignItems='CENTER';return wrap;}
function shell(p,role,active){const row=auto(p,'Application shell','HORIZONTAL',1440,1080,'canvas',0);const side=instance(row,'Sidebar/'+role);const col=auto(row,'Workspace','VERTICAL',1200,1080,null,0);const head=auto(col,'Workspace header','HORIZONTAL',1200,80,'white',16,24);head.counterAxisAlignItems='CENTER';text(head,role==='Staff'?'Không gian nhân viên':'Không gian quản trị',14,'Medium','muted');spacer(head);icon(head,'search');icon(head,'bell');const avatar=auto(head,'Avatar','HORIZONTAL',36,36,'orangeSoft',0);radius(avatar,12);avatar.primaryAxisAlignItems='CENTER';avatar.counterAxisAlignItems='CENTER';text(avatar,role==='Staff'?'MA':'AK',12,'Bold','orange');text(head,role==='Staff'?'Minh Anh':'Anh Khang',13,'SemiBold');const body=auto(col,'Main content','VERTICAL',1200,1000,null,24,32);return {row,side,head,body};}
function titleBlock(p,eyebrow,title,sub,action){const r=auto(p,'Page title','HORIZONTAL',1136,null,null,16);r.counterAxisAlignItems='CENTER';const c=auto(r,'Title','VERTICAL',null,null,null,4);text(c,eyebrow,11,'SemiBold','orange');text(c,title,30,'Bold');if(sub)text(c,sub,13,'Regular','muted');spacer(r);if(action)btn(r,action);return r;}
function table(p,key,headers,rows,widths,w=1136){const box=panel(p,'Table/'+key,w,null,0);gap(box,0);const hr=auto(box,'Table header','HORIZONTAL',w,48,'canvas',0,16);hr.counterAxisAlignItems='CENTER';headers.forEach((h,i)=>text(hr,h,11,'SemiBold','muted',widths[i]));for(const row of rows){const r=auto(box,'Row/'+row[0],'HORIZONTAL',w,70,'white',0,16);r.counterAxisAlignItems='CENTER';row.forEach((v,i)=>{const cell=auto(r,'Cell/'+headers[i],'VERTICAL',widths[i],null,null,0);if(typeof v==='object')badge(cell,v.label,v.tone);else text(cell,v,12,i===0?'SemiBold':'Regular',i===row.length-1?'orange':'ink',widths[i]-8);});line(box,w);}return box;}
const COMPS = {};
if(typeof S!=='undefined'&&S.components){for(const[k,id]of Object.entries(S.components)){COMPS[k]=await figma.getNodeByIdAsync(id);}}
