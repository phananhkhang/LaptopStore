const lib=await figma.getNodeByIdAsync(S.libraryId),components={};
for(const type of ['Primary','Secondary','Ghost','Disabled']){const c=COMPS['Button/'+type];c.paddingLeft=c.paddingRight=12;c.paddingTop=c.paddingBottom=12;ids.push(c.id);}
const navMap={'Tổng quan':'grid','Đơn hàng':'orders','Kho hàng':'box','Nhập hàng':'truck','Nhà cung cấp':'truck','Thanh toán':'wallet','Đánh giá':'star','Sản phẩm':'laptop','Danh mục':'grid','Thương hiệu':'tag','Khách hàng & nhân sự':'users','Khuyến mãi':'tag','Báo cáo':'chart','Chọn vai trò':'logout'};
const navComps={},variants=[];
for(const [label,ic]of Object.entries(navMap))for(const state of ['Default','Active']){
 const active=state==='Active',c=component('Item='+label+', State='+state,208,44,'HORIZONTAL',active?'orangeSoft':null,12,12);radius(c,8);c.counterAxisAlignItems='CENTER';icon(c,ic,active?'orange':'muted',20);const t=text(c,label,12,'SemiBold',active?'orange':'muted');prop(c,t,'Label',label);navComps[label+'/'+state]=c;components['Navigation/'+label+'/'+state]=c.id;variants.push(c);
}
const ns=made(figma.combineAsVariants(variants,figma.currentPage));ns.name='Navigation / Item & State';ns.description='Role navigation with distinct editable vector icons. Active and default variants use the same semantic tokens.';lib.appendChild(ns);ns.resize(1332,316);ns.children.forEach((n,i)=>{n.x=12+(i%6)*220;n.y=12+Math.floor(i/6)*60;});
const masters=[COMPS['Sidebar/Staff'],COMPS['Sidebar/Admin']];
for(const parent of [...masters,...await Promise.all(Object.values(S.screens).map(id=>figma.getNodeByIdAsync(id)))]){
 for(const n of parent.findAll(n=>n.type==='INSTANCE'&&n.name.startsWith('Link/'))){
  const c=await n.getMainComponentAsync();if(!c)continue;const isOld=c.id===S.components['Nav/Default']||c.id===S.components['Nav/Active'];if(!isOld)continue;const label=n.name.slice(5),state=c.id===S.components['Nav/Active']?'Active':'Default';if(navComps[label+'/'+state]){n.swapComponent(navComps[label+'/'+state]);props(n,{Label:label});n.name='Link/'+label;ids.push(n.id);}
 }
}
for(const root of await Promise.all(Object.values(S.screens).map(id=>figma.getNodeByIdAsync(id)))){
 for(const h of root.findAll(n=>n.name==='Workspace header'&&n.type==='FRAME')){
  const old=h.children.find(n=>n.name==='Icon/bell');if(old){const index=h.children.indexOf(old),n=icon(h,'bell','muted',22);h.insertChild(index,n);ids.push(old.id);old.remove();}
 }
 for(const n of root.findAll(n=>n.type==='INSTANCE'&&n.name==='Action/Xem'&&n.parent.name==='Card action')){n.paddingLeft=n.paddingRight=10;n.resize(60,36);ids.push(n.id);}
 for(const t of root.findAllWithCriteria({types:['TEXT']})){if(t.characters.startsWith('↗ ')){t.characters=t.characters.replace(/^↗ /,'+');ids.push(t.id);}}
}
const unresolved=await figma.getNodeByIdAsync('10:1003');await unresolved.setReactionsAsync([{trigger:{type:'ON_CLICK'},actions:[{type:'NODE',destinationId:S.screens.catalog,navigation:'NAVIGATE',transition:{type:'DISSOLVE',easing:{type:'EASE_OUT'},duration:.18},resetScrollPosition:true}]}]);ids.push(unresolved.id);
return {components,createdNodeIds:ids,mutatedNodeIds:[ns.id,...masters.map(n=>n.id)],variantCount:28};
