const PRODUCTS=[
 {id:'rog16',name:'ASUS ROG Strix G15',tag:'GAMING / ASUS',spec:'16GB · 512GB · Đen',sku:'ROG-G15-01',price:25000000,cpu:'Intel Core i7',gpu:'NVIDIA GeForce RTX 3060',screen:'15.6” · Full HD · 144Hz',memory:'16GB RAM · SSD 512GB',gaming:true,rog:true,description:'Hiệu năng mạnh mẽ cho lập trình, sáng tạo và những trận game mượt mà.'},
 {id:'rog32',name:'ASUS ROG Strix G15',tag:'GAMING / ASUS',spec:'32GB · 1TB · Đen',sku:'ROG-G15-02',price:30000000,cpu:'Intel Core i7',gpu:'NVIDIA GeForce RTX 3060',screen:'15.6” · Full HD · 144Hz',memory:'32GB RAM · SSD 1TB',gaming:true,rog:true,description:'Thêm không gian cho ý tưởng lớn. RAM 32GB và SSD 1TB cho công việc đa nhiệm.'},
 {id:'dell',name:'Dell XPS 13',tag:'MỎNG NHẸ / DELL',spec:'16GB · 512GB · Bạc',sku:'XPS13-01',price:35000000,cpu:'Intel Core i7',gpu:'Intel Iris Xe Graphics',screen:'13.4” · Full HD+ · 60Hz',memory:'16GB RAM · SSD 512GB',gaming:false,rog:false,description:'Thiết kế tinh gọn, màn hình sắc nét. Đồng hành cùng những ngày làm việc linh hoạt.'},
 {id:'vivo',name:'ASUS Vivobook 15',tag:'VĂN PHÒNG / ASUS',spec:'16GB · 512GB · Bạc',sku:'VVB15-16-512',price:16990000,cpu:'Intel Core i5',gpu:'Intel Iris Xe Graphics',screen:'15.6” · Full HD · 60Hz',memory:'16GB RAM · SSD 512GB',gaming:false,rog:false,description:'Gọn gàng cho học tập và công việc hằng ngày. Cấu hình cân bằng trong tầm giá.'},
 {id:'tuf',name:'ASUS TUF Gaming F15',tag:'GAMING / ASUS',spec:'16GB · 1TB · Đen',sku:'TUF-F15-16-1T',price:23490000,cpu:'Intel Core i7',gpu:'NVIDIA GeForce RTX 3050',screen:'15.6” · Full HD · 144Hz',memory:'16GB RAM · SSD 1TB',gaming:true,rog:false,description:'Sẵn sàng bứt phá. Lựa chọn gaming bền bỉ cho học tập và giải trí.'},
 {id:'zen',name:'ASUS Zenbook 14',tag:'MỎNG NHẸ / ASUS',spec:'16GB · 512GB · Bạc',sku:'ZEN14-16-512',price:24990000,cpu:'Intel Core i7',gpu:'Intel Iris Xe Graphics',screen:'14” · 2.8K OLED · 90Hz',memory:'16GB RAM · SSD 512GB',gaming:false,rog:false,description:'Màn hình đẹp, thiết kế nhẹ. Mang cảm hứng sáng tạo đến mọi nơi.'},
 {id:'loq',name:'Lenovo LOQ 15',tag:'GAMING / LENOVO',spec:'16GB · 512GB · Đen',sku:'LOQ15-16-512',price:22990000,cpu:'Intel Core i5',gpu:'NVIDIA GeForce RTX 3050',screen:'15.6” · Full HD · 144Hz',memory:'16GB RAM · SSD 512GB',gaming:true,rog:false,description:'Hiệu năng cân bằng cho lập trình và chơi game. Sẵn sàng cho nhịp học tập mới.'}
];
function money(n){return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,'.')+'₫';}
function values(p,q=1,coupon=true){const sub=p.price*q,discount=coupon&&sub>=20000000?500000:0;return {id:p.id,name:p.name,tag:p.tag,spec:p.spec,sku:p.sku,skuLabel:'SKU '+p.sku,skuInfo:p.spec+' · SKU '+p.sku,price:money(p.price),subtotal:money(sub),total:money(sub-discount),discount:discount?'−'+money(discount):'0₫',discountLabel:discount?'Giảm giá LAPTOP500':'Giảm giá',quantity:String(q),qtyLabel:'Số lượng: '+q,itemSummary:p.spec+' · Số lượng: '+q,cpu:p.cpu,gpu:p.gpu,screen:p.screen,memory:p.memory,description:p.description,isGaming:p.gaming,isSilver:!p.gaming,isRog:p.rog,couponApplied:discount>0,couponWarning:coupon&&discount===0,couponEnabled:coupon,couponMessage:discount?'Đã giảm 500.000₫ cho đơn hàng này':'Đơn hàng từ 20 triệu mới áp dụng LAPTOP500'};}
function dataActions(prefix,p,q=1,coupon=true){return Object.entries(values(p,q,coupon)).map(([k,v])=>setVar(prefix+'/'+k,v));}
function bindMap(root,prefix,map){for(const t of root.findAllWithCriteria({types:['TEXT']}))if(map[t.characters])bind(t,prefix+'/'+map[t.characters]);}
function bindPurchase(root,prefix='cart'){
 const sums=root.findAll(n=>n.name==='Order summary'||n.name==='Payment order summary');
 for(const box of sums){for(const row of box.children.filter(n=>n.layoutMode==='HORIZONTAL')){if(row.name==='Tạm tính'){const t=row.findAllWithCriteria({types:['TEXT']}).at(-1);if(t)bind(t,prefix+'/subtotal');}if(row.name==='Giảm giá LAPTOP500'){const ts=row.findAllWithCriteria({types:['TEXT']});if(ts[0])bind(ts[0],prefix+'/discountLabel');if(ts[1])bind(ts[1],prefix+'/discount');}}
  const ts=box.findAllWithCriteria({types:['TEXT']});for(const t of ts)if(t.characters==='24.500.000₫'||t.characters==='24.500.000 ₫')bind(t,prefix+'/total');
 }
 for(const t of root.findAllWithCriteria({types:['TEXT']})){
  if(t.characters==='ASUS ROG Strix G15')bind(t,prefix+'/name');
  else if(['16GB · 512GB · Đen','16 GB · 512 GB SSD · Đen'].includes(t.characters))bind(t,prefix+'/spec');
  else if(t.characters==='SKU ROG-G15-01')bind(t,prefix+'/skuLabel');
  else if(t.characters==='16GB / 512GB / Đen  ·  Số lượng: 1')bind(t,prefix+'/itemSummary');
  else if(t.characters==='25.000.000₫'||t.characters==='25.000.000 ₫')bind(t,t.parent.name==='Tạm tính'?prefix+'/subtotal':prefix+'/price');
  else if(t.characters==='−500.000₫'||t.characters==='−500.000 ₫')bind(t,prefix+'/discount');
 }
}
