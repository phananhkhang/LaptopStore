const lib=await figma.getNodeByIdAsync(S.libraryId);
const comps={};
function register(key,c){comps[key]=c.id;COMPS[key]=c;return c;}
// Adapted from createComponentWithVariants.js: fixed matrix and explicit grid placement.
const variants=[];
for(const [i,type]of ['Primary','Secondary','Ghost','Disabled'].entries()){
 const c=component('Style='+type,180,48,'HORIZONTAL',type==='Primary'?'orange':type==='Disabled'?'canvas':'white',8,16);c.primaryAxisAlignItems='CENTER';c.counterAxisAlignItems='CENTER';radius(c,12);if(type==='Secondary')border(c);
 const t=text(c,'Tiếp tục',14,'SemiBold',type==='Primary'?'white':type==='Disabled'?'muted':'ink');prop(c,t,'Label','Tiếp tục');variants.push(c);register('Button/'+type,c);
}
const set=made(figma.combineAsVariants(variants,figma.currentPage));set.name='Button';set.description='Primary for the main action; secondary for alternatives; ghost for navigation. Label is editable.';lib.appendChild(set);set.resize(840,88);set.children.forEach((c,i)=>{c.x=20+i*204;c.y=20;});
const badgeRow=auto(lib,'Status badges','HORIZONTAL',null,null,null,16);
for(const tone of ['Orange','Green','Blue','Red','Neutral']){const k=tone.toLowerCase();const c=component('Badge/'+tone,124,28,'HORIZONTAL',tone==='Neutral'?'canvas':k+'Soft',0,8);c.primaryAxisSizingMode='AUTO';c.counterAxisSizingMode='AUTO';c.primaryAxisAlignItems='CENTER';c.counterAxisAlignItems='CENTER';radius(c,8);const t=text(c,'Đang xử lý',11,'SemiBold',tone==='Neutral'?'muted':k);prop(c,t,'Label','Đang xử lý');badgeRow.appendChild(c);register('Badge/'+tone,c);}
const input=component('Input',340,78,'VERTICAL',null,8,0);const il=text(input,'Nhãn trường',12,'SemiBold');prop(input,il,'Label','Nhãn trường');const ib=auto(input,'Input control','HORIZONTAL',340,48,'white',8,12);radius(ib,8);border(ib);ib.counterAxisAlignItems='CENTER';const iv=text(ib,'Giá trị',13,'Regular','muted');prop(input,iv,'Value','Giá trị');lib.appendChild(input);register('Input',input);
const m=component('Metric',268,132,'VERTICAL','white',8,20);radius(m,16);border(m);const mt=text(m,'Doanh thu',12,'Regular','muted');prop(m,mt,'Title','Doanh thu');const mv=text(m,'1,28 tỷ',28,'Bold');prop(m,mv,'Value','1,28 tỷ');const md=text(m,'+12,8% so với kỳ trước',11,'SemiBold','green');prop(m,md,'Delta','+12,8% so với kỳ trước');lib.appendChild(m);register('Metric',m);
const navs=[];for(const active of [false,true]){const c=component('State='+(active?'Active':'Default'),208,44,'HORIZONTAL',active?'orangeSoft':null,12,12);radius(c,8);c.counterAxisAlignItems='CENTER';icon(c,'grid',active?'orange':'muted',20);const t=text(c,'Tổng quan',12,'SemiBold',active?'orange':'muted');prop(c,t,'Label','Tổng quan');navs.push(c);register('Nav/'+(active?'Active':'Default'),c);}
const ns=made(figma.combineAsVariants(navs,figma.currentPage));ns.name='Navigation item';lib.appendChild(ns);ns.resize(480,84);ns.children.forEach((c,i)=>{c.x=16+i*232;c.y=16;});
const header=component('Shop header',1440,120,'VERTICAL','white',0,0);
const top=auto(header,'Utility bar','HORIZONTAL',1440,30,'orangePale',16,0);top.paddingLeft=80;top.paddingRight=80;top.counterAxisAlignItems='CENTER';text(top,'Công nghệ tốt hơn. Mỗi ngày.',11,'Medium','orange');spacer(top);text(top,'Giao hàng toàn quốc',11,'Regular','muted');text(top,'Hotline 1800 2026',11,'SemiBold','muted');
const hr=auto(header,'Main navigation','HORIZONTAL',1440,90,'white',32,0);hr.paddingLeft=80;hr.paddingRight=80;hr.counterAxisAlignItems='CENTER';
const logo=auto(hr,'Brand','HORIZONTAL',null,null,null,8);logo.counterAxisAlignItems='CENTER';const mark=auto(logo,'Brand mark','HORIZONTAL',34,34,'orange',0);radius(mark,8);mark.primaryAxisAlignItems='CENTER';mark.counterAxisAlignItems='CENTER';icon(mark,'box','white',22);text(logo,'laptopstore',20,'Bold');
const hn=auto(hr,'Shop links','HORIZONTAL',null,null,null,24);for(const label of ['Laptop','Khuyến mãi','Tư vấn AI']){const t=text(hn,label,13,'SemiBold',label==='Laptop'?'orange':'ink');t.name='Link/'+label;}
const search=auto(hr,'Search','HORIZONTAL',320,44,'canvas',12,12);radius(search,12);icon(search,'search','muted',20);text(search,'Tìm chiếc laptop của bạn...',12,'Regular','muted');spacer(hr);const acc=icon(hr,'user','ink');acc.name='Link/Tài khoản';const cart=icon(hr,'cart','ink');cart.name='Link/Giỏ hàng';lib.appendChild(header);register('Shop header',header);
for(const role of ['Staff','Admin']){
 const side=component('Sidebar/'+role,240,1080,'VERTICAL','white',8,16);border(side);const br=auto(side,'Brand','HORIZONTAL',208,64,null,8,8);br.counterAxisAlignItems='CENTER';icon(br,'box','orange',26);text(br,'laptopstore',18,'Bold');const roleP=auto(side,'Role label','HORIZONTAL',208,40,null,0,12);text(roleP,role==='Staff'?'VẬN HÀNH CỬA HÀNG':'QUẢN TRỊ HỆ THỐNG',10,'SemiBold','muted');
 const items=role==='Staff'?['Tổng quan','Đơn hàng','Kho hàng','Nhập hàng','Nhà cung cấp','Thanh toán','Đánh giá']:['Tổng quan','Sản phẩm','Danh mục','Thương hiệu','Đơn hàng','Kho hàng','Khách hàng & nhân sự','Khuyến mãi','Nhà cung cấp','Báo cáo'];
 for(const label of items){const n=instance(side,'Nav/Default',{Label:label});n.name='Link/'+label;}
 const free=auto(side,'Spacer','VERTICAL',208,20,null,0);free.layoutSizingVertical='FILL';
 const note=auto(side,'Workspace help','VERTICAL',208,null,'orangePale',8,16);radius(note,12);icon(note,'headset','orange',24);text(note,'Một ngày thật hiệu quả',12,'SemiBold');text(note,'Mọi thao tác, cùng một nơi.',11,'Regular','muted');
 const switcher=instance(side,'Nav/Default',{Label:'Chọn vai trò'});switcher.name='Link/Chọn vai trò';lib.appendChild(side);register('Sidebar/'+role,side);
}
return {components:comps,createdNodeIds:ids,counts:{components:Object.keys(comps).length,buttonVariants:4,navVariants:2},libraryBounds:{width:lib.width,height:lib.height},bindings:{buttonFill:COMPS['Button/Primary'].fills[0].boundVariables,buttonRadius:COMPS['Button/Primary'].boundVariables}};
