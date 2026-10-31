const groups=[
 ['01 · Khách hàng / 23 màn hình',['home','catalog','detail','cart','emptyCart','checkout','success','payPending','paySuccess','payFail','orders','tracking','review','profile','addresses','addressEdit','password','login','register','ai','emptySearch','forbidden','notFound']],
 ['02 · Staff / 19 màn hình',['staffOverview','staff','staffDetail','staffConfirmed','staffProcessing','staffShipping','staffDelivered','inventory','stockHistory','purchaseOrders','purchaseEdit','purchaseReceive','purchaseDone','staffSuppliers','payments','paymentDetail','codConfirmed','moderation','moderationHidden']],
 ['03 · Admin / 14 màn hình',['admin','products','productEdit','categories','brands','users','userDetail','couponList','couponEdit','couponUsage','suppliers','supplierEdit','stockAdjust','report']]
];
const sections={},positions=[];let y=2100,index=1;
for(const [name,keys]of groups){
 const section=made(figma.createSection());section.name=name;section.fills=[{type:'SOLID',color:rgb('#EEF0F3')}];section.x=0;section.y=y;
 let rowY=80;
 for(let i=0;i<keys.length;i+=4){const row=keys.slice(i,i+4);let maxHeight=0;for(let j=0;j<row.length;j++){const key=row[j],root=await screen(key);section.appendChild(root);root.name=String(index++).padStart(2,'0')+' · '+root.name.replace(/^\d+\s*·\s*/,'');root.x=64+j*1560;root.y=rowY;maxHeight=Math.max(maxHeight,root.height);ids.push(root.id);positions.push({key,id:root.id,name:root.name,width:root.width,height:root.height});}rowY+=maxHeight+160;}
 section.resizeWithoutConstraints(6248,rowY-80);sections[name]=section.id;y+=section.height+240;
}
const launch=await screen('start');launch.x=0;launch.y=0;launch.name='00 · Bắt đầu / 57 màn hình & 3 vai trò';ids.push(launch.id);
const coverLabel=launch.findOne(n=>n.type==='TEXT'&&n.characters==='DESIGN SYSTEM / 2026');if(coverLabel){coverLabel.characters='57 MÀN HÌNH / 3 VAI TRÒ / 2026';ids.push(coverLabel.id);}
const lib=await figma.getNodeByIdAsync(S.libraryId);lib.resize(1600,lib.height);lib.layoutSizingVertical='HUG';lib.x=6540;lib.y=2100;ids.push(lib.id);
const foundations=auto(lib,'Foundations / Color & typography','VERTICAL',1504,null,null,24);const swatches=auto(foundations,'Semantic color palette','HORIZONTAL',1504,null,null,16);swatches.layoutWrap='WRAP';
for(const key of ['orange','orangeDark','orangeSoft','orangePale','ink','muted','line','canvas','green','greenSoft','blue','blueSoft','red','redSoft']){const box=auto(swatches,'Token / '+key,'VERTICAL',170,null,null,8);const fillBox=auto(box,'Color / '+key,'VERTICAL',170,64,key,0);radius(fillBox,12);text(box,key,11,'SemiBold');text(box,palette[key],10,'Regular','muted');}
text(foundations,'Kiểu chữ / Be Vietnam Pro',28,'Bold');text(foundations,'Tìm cảm hứng cho điều bạn muốn.',36,'Bold','ink',1400);text(foundations,'Aa Ă Â Đ Ê Ô Ơ Ư · 0123456789 · 24.500.000 ₫',20,'Bold');text(foundations,'Chữ tiếng Việt rõ ràng. Nội dung có thể sửa trực tiếp trong Figma.',14,'Regular','muted');text(foundations,'Spacing 4 / 8 / 12 / 16 / 24 / 32 / 48 / 64 · Radius 8 / 12 / 16 / 20 / 24',12,'Regular','muted');lib.insertChild(2,foundations);
figma.currentPage.selection=[await screen('home')];figma.viewport.scrollAndZoomIntoView([await screen('home')]);
return {createdNodeIds:ids,mutatedNodeIds:positions.map(p=>p.id),sections,positions,libraryId:lib.id,librarySize:[lib.width,lib.height],totalScreens:positions.length+1};
