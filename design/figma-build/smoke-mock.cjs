'use strict';
const fs=require('node:fs');
const path=require('node:path');
const {createMockFigma,serializeNode}=require('./mock-figma.cjs');
const AsyncFunction=Object.getPrototypeOf(async function(){}).constructor;
(async()=>{
  const figma=createMockFigma();
  const helpers=fs.readFileSync(path.join(__dirname,'helpers.js'),'utf8');
  const assetDir=path.join(__dirname,'../figma-assets');
  const ICONS=JSON.parse(fs.readFileSync(path.join(assetDir,'icons.json'),'utf8'));
  const ASSETS=Object.fromEntries(fs.readdirSync(assetDir).filter(f=>f.endsWith('.svg')).map(f=>[f.slice(0,-4),fs.readFileSync(path.join(assetDir,f),'utf8')]));
  let S={};
  const scriptResults=[];
  for(const file of ['foundations.js','components.js','assets-expand.js','core-screens.js','customer-secondary.js','ops-secondary.js','prototype.js']){
    if(!fs.existsSync(path.join(__dirname,file)))continue;
    const result=await new AsyncFunction('figma','S','ICONS','ASSETS',helpers+'\n'+fs.readFileSync(path.join(__dirname,file),'utf8'))(figma,S,ICONS,ASSETS);
    S={...S,...result,screens:{...S.screens,...result?.screens},components:{...S.components,...result?.components}};
    scriptResults.push({script:file,nodeCount:figma._nodes.size,createdNodeCount:result?.createdNodeIds?.length||0});
    console.log(file+': '+figma._nodes.size+' nodes');
  }
  const serialized=serializeNode(figma.currentPage);
  const json=JSON.stringify(serialized);
  const screens=Object.values(S.screens).map(id=>figma.getNodeById(id));
  console.log(JSON.stringify({nodes:figma._nodes.size,screens:screens.length,components:Object.keys(S.components).length,variables:figma._vars.size,serializedBytes:json.length,filledScreens:screens.filter(n=>n.children.length>0).length}));
  if(process.argv.includes('--write')){
    const designDir=path.join(__dirname,'..');
    fs.writeFileSync(path.join(designDir,'preview-tree.json'),json);
    fs.writeFileSync(path.join(designDir,'preview-state.json'),JSON.stringify(S,null,2));
    const tolerance=3;
    const issueList=[],assetOverflow=[],screenBounds=[];
    const svgDescendant=n=>!!n.originalSvg||n.mainComponentName?.startsWith('Asset/');
    const inspect=(n,screen,pathParts,insideAsset=false)=>{
      if(n.visible===false)return;
      const childPath=[...pathParts,n.name];
      const asset=insideAsset||svgDescendant(n);
      for(const c of n.children||[]){
        if(c.visible===false)continue;
        const overflow={left:Math.max(0,-c.x),top:Math.max(0,-c.y),right:Math.max(0,c.x+c.width-n.width),bottom:Math.max(0,c.y+c.height-n.height)};
        const amount=Math.max(...Object.values(overflow));
        if(amount>tolerance){
          const issue={screen:screen.name,screenId:screen.id,parent:n.name,parentId:n.id,child:c.name,childId:c.id,path:[...childPath,c.name].join(' / '),overflow:Object.fromEntries(Object.entries(overflow).map(([k,v])=>[k,Math.round(v*10)/10])),parentBounds:{width:n.width,height:n.height},childBounds:{x:c.x,y:c.y,width:c.width,height:c.height},parentClipsContent:!!n.clipsContent};
          (asset?assetOverflow:issueList).push(issue);
        }
        inspect(c,screen,childPath,asset);
      }
    };
    const keyById=Object.fromEntries(Object.entries(S.screens).map(([k,id])=>[id,k]));
    for(const n of serialized.children.filter(n=>keyById[n.id])){
      screenBounds.push({key:keyById[n.id],id:n.id,name:n.name,x:n.x,y:n.y,width:n.width,height:n.height,children:n.children.length,placeholder:!!n.placeholder});
      inspect(n,n,[]);
    }
    const audit={approximation:'Offline mock of Figma auto layout and Be Vietnam Pro text metrics. Library excluded. Values are preview QA hints; fixed viewport vertical overflow may require scrolling.',scripts:scriptResults,summary:{screens:screens.length,emptyScreens:screenBounds.filter(n=>!n.children).length,placeholderScreens:screenBounds.filter(n=>n.placeholder).length,contentOverflowIssues:issueList.length,assetOverflowIssues:assetOverflow.length},screenBounds,empty:screenBounds.filter(n=>!n.children),placeholder:screenBounds.filter(n=>n.placeholder),overflow:issueList,assetOverflow};
    fs.writeFileSync(path.join(designDir,'layout-audit.json'),JSON.stringify(audit,null,2));
    console.log('Audit: '+JSON.stringify(audit.summary));
  }
})().catch(error=>{console.error(error);process.exitCode=1;});
