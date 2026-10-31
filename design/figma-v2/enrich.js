const lib=await figma.getNodeByIdAsync(S.libraryId), components={};
// A shared, editable footer belongs to the actual storefront, not the review shell.
const footer=component('Shop footer',1440,300,'VERTICAL','canvas',28,40);footer.paddingLeft=footer.paddingRight=80;
const columns=auto(footer,'Footer columns','HORIZONTAL',1280,null,null,40);
const brand=auto(columns,'Brand promise','VERTICAL',340,null,null,12);
const brandRow=auto(brand,'Logo','HORIZONTAL',340,null,null,10);icon(brandRow,'laptop','orange',28);text(brandRow,'laptopstore',20,'Bold');
text(brand,'Cùng bạn tìm chiếc laptop phù hợp, để mỗi ngày có thêm một ý tưởng mới.',12,'Regular','muted',300);
const promise=auto(brand,'Trust','HORIZONTAL',320,null,null,8);icon(promise,'shield','orange',18);text(promise,'Chính hãng · Tư vấn tận tâm',11,'Medium','orange');
for(const [heading,items]of [['Khám phá LaptopStore',[['Tất cả laptop','catalog'],['Laptop gaming','catalog'],['Laptop văn phòng','catalog'],['Tư vấn cùng AI','ai']]],['Chăm sóc khách hàng',[['Đơn hàng của tôi','orders'],['Thông tin tài khoản','profile'],['Sổ địa chỉ','addresses'],['Đăng nhập','login']]]]){
 const c=auto(columns,heading,'VERTICAL',240,null,null,12);text(c,heading,12,'SemiBold');for(const[label,key]of items){const t=text(c,label,12,'Regular','muted');t.name='Screen/'+key;}
}
const support=auto(columns,'Payment and delivery','VERTICAL',280,null,null,12);text(support,'Thanh toán an tâm',12,'SemiBold');text(support,'COD  ·  VNPay',12,'Regular','muted');text(support,'Giao hàng toàn quốc\nHỗ trợ từ 8:00 đến 21:00\nHotline 1800 2026',12,'Regular','muted',280);
line(footer,1280);text(footer,'© 2026 LaptopStore. Công nghệ cho điều bạn muốn.',10,'Regular','muted');footer.description='Reusable storefront footer. Real text and vector icons, linked to account and shopping screens.';lib.appendChild(footer);COMPS['Shop footer']=footer;components['Shop footer']=footer.id;

const home=await screen('home'),body=home.children.find(n=>n.name==='Page content');
const collections=auto(body,'Collections / Work and play','HORIZONTAL',1280,280,null,24);
for(const [tag,title,desc,link,asset,bg]of [['WORK ANYWHERE','Nhẹ hành trang.\nRộng cảm hứng.','Không gian làm việc theo bạn đến mọi nơi.','Laptop mỏng nhẹ','product-silver','orangePale'],['PLAY YOUR WAY','Sẵn sàng cho\ntrận đấu lớn.','Cấu hình mạnh. Từng khung hình mượt mà.','Khám phá gaming','product-dark','canvas']]){
 const c=auto(collections,title,'HORIZONTAL',628,280,bg,0,28);radius(c,20);c.counterAxisAlignItems='CENTER';const copy=auto(c,'Collection copy','VERTICAL',260,null,null,12);text(copy,tag,10,'SemiBold','orange');text(copy,title,24,'Bold','ink',260);text(copy,desc,12,'Regular','muted',260);const l=text(copy,link+' →',13,'SemiBold','orange');l.name='Screen/catalog';const pic=instance(c,'Asset/'+asset);pic.rescale(310/480);
}
const productIndex=body.children.findIndex(n=>n.name==='Products');body.insertChild(productIndex+1,collections);
const heroText=body.findOne(n=>n.type==='TEXT'&&n.characters==='Chiếc laptop cho\nmọi ý tưởng.');if(heroText){const i=heroText.characters.indexOf('ý tưởng');heroText.setRangeFills(i,heroText.characters.length,[paint('orange')]);ids.push(heroText.id);}
for(const [key,changes]of [['ProductCard/rog32',{'ROG Strix G15 / 32GB':'ASUS Vivobook 15','i7 · RTX 3060 · 32GB · 1TB':'Core i5 · 16GB · 512GB','30.000.000₫':'16.990.000₫','HIỆU NĂNG':'VĂN PHÒNG'}],['ProductCard/office',{'Laptop cho ngày sáng tạo':'ASUS Zenbook 14','Màn hình đẹp · Pin cả ngày':'Core i7 · 16GB · 512GB','Khám phá bộ sưu tập':'24.990.000₫','VĂN PHÒNG':'MỎNG NHẸ'}]]){
 const c=COMPS[key];for(const t of c.findAllWithCriteria({types:['TEXT']})){if(changes[t.characters]){t.characters=changes[t.characters];ids.push(t.id);}}
 if(key.endsWith('rog32')){const pic=c.findOne(n=>n.type==='INSTANCE'&&n.name==='Asset/product-dark');if(pic){pic.swapComponent(COMPS['Asset/product-silver']);pic.rescale(252/pic.width);ids.push(pic.id);}}
}
for(const k of ['ProductCard/rog','ProductCard/dell','ProductCard/rog32','ProductCard/office']){const c=COMPS[k];c.resize(302,390);const title=c.findOne(n=>n.type==='TEXT'&&n.fontSize===16);const price=c.findOne(n=>n.type==='TEXT'&&n.fontSize===18);const specs=c.findOne(n=>n.type==='TEXT'&&n.fontSize===11&&!n.characters.startsWith('★'));if(title)prop(c,title,'Title',title.characters);if(price)prop(c,price,'Price',price.characters);if(specs)prop(c,specs,'Specs',specs.characters);ids.push(c.id);}
const customer=['home','catalog','detail','cart','checkout','success','payPending','payFail','paySuccess','orders','tracking','review','profile','addresses','addressEdit','password','login','register','ai','emptyCart','emptySearch','forbidden','notFound'];
for(const key of customer){const root=await screen(key);fill(root,'white');const content=root.children.find(n=>n.name==='Page content');if(content)content.paddingBottom=48;instance(root,'Shop footer');root.minHeight=1080;root.layoutSizingVertical='HUG';ids.push(root.id);}

// Make all 57 requested states directly discoverable from the prototype launcher.
const launch=await screen('start');const index=auto(launch,'Screen directory / 57 views','HORIZONTAL',1280,null,null,24);
const groups=[['Khách hàng',customer],['Vận hành',['staffOverview','staff','staffDetail','staffConfirmed','staffProcessing','staffShipping','staffDelivered','inventory','stockHistory','purchaseOrders','purchaseEdit','purchaseReceive','purchaseDone','staffSuppliers','payments','paymentDetail','codConfirmed','moderation','moderationHidden']],['Quản trị',['admin','products','productEdit','categories','brands','users','userDetail','couponList','couponEdit','couponUsage','suppliers','supplierEdit','stockAdjust','report']]];
for(const [title,keys]of groups){const c=panel(index,title,410,null,24);text(c,title+' / '+keys.length+' màn',18,'Bold');for(const key of keys){const screenNode=await screen(key);const label=screenNode.name.replace(/^\d+\s*·\s*/,'').replace(/^(User|Auth|System|Staff|Admin)\s*\/\s*/,'');const t=text(c,label,12,'Medium','muted',362);t.name='Screen/'+key;}}
launch.layoutSizingVertical='HUG';launch.minHeight=1080;ids.push(launch.id);
const pending=await screen('payPending');const branch=pending.findOne(n=>n.name==='Payment status');if(branch){const t=text(branch,'Xem trường hợp giao dịch thất bại',11,'Medium','muted');t.name='Screen/payFail';}
return {components,createdNodeIds:ids,mutatedNodeIds:ids,home:{width:home.width,height:home.height},launcher:{width:launch.width,height:launch.height}};
