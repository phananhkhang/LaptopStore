const fs=require('fs'), path=require('path');
const out=__dirname, src=path.join(out,'../figma-build');
for(const f of ['helpers.js','foundations.js','components.js','assets-expand.js','core-screens.js','customer-secondary.js','ops-secondary.js','prototype.js']){
 let s=fs.readFileSync(path.join(src,f),'utf8').replace(/\b[a-zA-Z_$][\w$]*\.placeholder\s*=\s*(?:true|false)\s*;/g,'');
 if(f==='helpers.js'){
  s=s.replace("orange:'#E65319',orangeDark:'#BD3D0D',orangeSoft:'#FFF1E8',orangePale:'#FFF8F3',ink:'#212226',muted:'#74777F',line:'#E9EAED'", "orange:'#C74615',orangeDark:'#A9360F',orangeSoft:'#FFF2E9',orangePale:'#FDF4EC',ink:'#202126',muted:'#62656D',line:'#E7E8EB'");
  s=s.replaceAll("vars['color/", "vars['v2/color/").replaceAll("styles['LaptopStore/", "styles['LaptopStore v2/");
  s=s.replace("n.fontName={family:'Be Vietnam Pro',style:weight};", "if(size>=18&&weight==='Bold')weight='SemiBold';n.fontName={family:'Be Vietnam Pro',style:weight};");
  s=s.replace("value:150", "value:size>=30?120:150");
  s=s.replace("if(w){n.textAutoResize='HEIGHT';n.resize(w,n.height);}return n;", "const max=p.layoutMode==='VERTICAL'&&p.layoutSizingHorizontal!=='HUG'?p.width-p.paddingLeft-p.paddingRight:0;if(w||(max>0&&n.width>max)){n.textAutoResize='HEIGHT';n.resize(w||max,n.height);}return n;");
  s=s.replace("if(w)n.resize(w,n.height);return n;", "if(w)n.resize(w,n.height);if(key.startsWith('Button/')){const t=n.findOne(q=>q.type==='TEXT');t.textAutoResize='HEIGHT';t.resize(Math.max(12,n.width-28),t.height);t.textAlignHorizontal='CENTER';n.resize(n.width,Math.max(44,t.height+20));}return n;");
  s=s.replace("w=250", "w=272");
  s=s.replace("1440,1080,'canvas'", "1440,null,'canvas'").replace("1200,1080,null", "1200,null,null").replace("1200,1000,null,24,32", "1200,null,null,24,32");
  s=s.replace("const body=auto(col,'Main content','VERTICAL',1200,null,null,24,32);", "const body=auto(col,'Main content','VERTICAL',1200,null,null,24,32);body.minHeight=1000;");
  s=s.replace("const c=auto(r,'Title','VERTICAL',null,null,null,4);", "const c=auto(r,'Title','VERTICAL',action?850:1100,null,null,4);");
  s=s.replace("function table(p,key,headers,rows,widths,w=1136){", "function table(p,key,headers,rows,widths,w=1136){const sum=widths.reduce((a,b)=>a+b,0);widths=widths.map(v=>v*(w-32)/sum);");
  s=s.replace("hr.counterAxisAlignItems='CENTER';headers", "hr.paddingTop=hr.paddingBottom=0;hr.counterAxisAlignItems='CENTER';headers");
  s=s.replace("r.counterAxisAlignItems='CENTER';row.forEach", "r.paddingTop=r.paddingBottom=0;r.counterAxisAlignItems='CENTER';row.forEach");
 }
 if(f==='foundations.js'){
  s=s.replace("const page=figma.currentPage;", "const page=figma.currentPage;").replace("LaptopStore · Full website & Prototype", "LaptopStore · 57 màn hình / Redesign 2026");
  s=s.replaceAll("LaptopStore / Primitives", "LaptopStore v2 / Primitives").replaceAll("LaptopStore / Theme", "LaptopStore v2 / Theme").replaceAll("'palette/", "'v2/palette/").replaceAll("'color/", "'v2/color/").replaceAll("'LaptopStore/", "'LaptopStore v2/");
  s=s.replace("for(const [size,weight]of styleData)", "for(const [size,weight]of [...styleData,...[18,20,22,24,28,30,36,40,52,56].map(n=>[n,'SemiBold'])])");
  s=s.replace("Library · Components & original vector assets", "UI Kit · Foundations / Components / Original assets");
 }
 if(f==='components.js'){
  s=s.replace("text(c,'Tiếp tục',14,'SemiBold'", "text(c,'Tiếp tục',13,'SemiBold'");
  s=s.replace("const iv=text(ib,'Giá trị',13,'Regular','muted');", "ib.layoutSizingHorizontal='FILL';const iv=text(ib,'Giá trị',13,'Regular','muted',316);iv.layoutSizingHorizontal='FILL';");
  s=s.replace("text(logo,'laptopstore',20,'Bold')", "text(logo,'laptopstore',20,'Bold')");
  s=s.replace("icon(mark,'box'", "icon(mark,'laptop'").replace("icon(br,'box'", "icon(br,'laptop'");
  s=s.replace("1440,120", "1440,116").replace("1440,90", "1440,86");
  s=s.replace("'white',32,0", "'white',28,0").replace("'HORIZONTAL',320,44", "'HORIZONTAL',300,44");
 }
 if(f==='core-screens.js'){
  s=s.replace("const n=side.findAllWithCriteria({types:['INSTANCE']}).find(n=>n.name==='Link/'+label);if(n)n.swapComponent(COMPS['Nav/Active']);", "const n=side.findAllWithCriteria({types:['INSTANCE']}).find(n=>n.name==='Link/'+label);if(n){n.swapComponent(COMPS['Nav/Active']);props(n,{Label:label});n.name='Link/'+label;}");
  s=s.replace("24);body.paddingTop=24", "32);body.paddingTop=28");
  s=s.replace("1280,408,'orangePale'", "1280,460,'orangePale'").replace("516,null,null,16", "516,null,null,20");
  s=s.replace("badge(copy,'BẬT SÁNG Ý TƯỞNG MỚI','Orange');", "text(copy,'MỞ MÁY LÊN. MỞ ĐIỀU MỚI.',11,'SemiBold','orange');");
  s=s.replace("simpleLink(acts,'Tư vấn cùng AI','orange');", "btn(acts,'Tư vấn cùng AI','Secondary',170);");
  s=s.replace("for(const c of ['Tất cả laptop','Gaming','Học tập & văn phòng','Mỏng nhẹ','Đồ họa'])btn(cats,c,c==='Tất cả laptop'?'Primary':'Secondary',220);", "for(const c of ['Tất cả laptop','Gaming','Văn phòng','Mỏng nhẹ'])btn(cats,c,c==='Tất cả laptop'?'Primary':'Secondary',152);");
  s=s.replace("text(chart,'01                                  07                                  14                                  21      /10',10,'Regular','muted');", "text(chart,'Đơn vị: triệu đồng · 01–24/10/2026',10,'Regular','muted');");
 }
 fs.writeFileSync(path.join(out,f),s);
}
console.log('Prepared Figma v2 authoring sources');
