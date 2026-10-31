// Local, original visual system for the requested white/orange redesign.
const page=figma.currentPage;
page.name='LaptopStore · Full website & Prototype';
const existingCollections=await figma.variables.getLocalVariableCollectionsAsync();
const createVariableCollection=(name,modeName)=>{const found=existingCollections.find(c=>c.name===name);if(found)return found;const c=figma.variables.createVariableCollection(name);c.renameMode(c.modes[0].modeId,modeName);return c;};
const primitives=createVariableCollection('LaptopStore / Primitives','Value');
const semantic=createVariableCollection('LaptopStore / Theme','Light');
const geometry=createVariableCollection('LaptopStore / Geometry','Value');
const variables={};
for(const[k,v]of Object.entries(palette)){
 const p=vars['palette/'+k]||figma.variables.createVariable('palette/'+k,primitives,'COLOR');p.scopes=[];p.setValueForMode(primitives.defaultModeId,rgb(v));p.setVariableCodeSyntax('WEB','var(--ls-palette-'+k+')');
 const t=vars['color/'+k]||figma.variables.createVariable('color/'+k,semantic,'COLOR');t.scopes=['FRAME_FILL','SHAPE_FILL','TEXT_FILL','STROKE_COLOR'];t.setValueForMode(semantic.defaultModeId,{type:'VARIABLE_ALIAS',id:p.id});t.setVariableCodeSyntax('WEB','var(--ls-color-'+k+')');variables[t.name]=t.id;vars[t.name]=t;
}
for(const [prefix,vals,scopes]of [['space',[0,4,8,12,16,20,24,32,40,48,64],['GAP']],['radius',[8,12,16,20,24,32],['CORNER_RADIUS']]])for(const val of vals){const v=vars[prefix+'/'+val]||figma.variables.createVariable(prefix+'/'+val,geometry,'FLOAT');v.scopes=scopes;v.setValueForMode(geometry.defaultModeId,val);v.setVariableCodeSyntax('WEB','var(--ls-'+prefix+'-'+val+')');variables[v.name]=v.id;vars[v.name]=v;}
const styleData=[[11,'Regular'],[11,'SemiBold'],[12,'Regular'],[12,'SemiBold'],[12,'Bold'],[13,'Regular'],[13,'Medium'],[13,'SemiBold'],[14,'Regular'],[14,'Medium'],[14,'SemiBold'],[14,'Bold'],[16,'Regular'],[16,'SemiBold'],[18,'Bold'],[20,'Bold'],[22,'Bold'],[24,'Bold'],[28,'Bold'],[30,'Bold'],[36,'Bold'],[52,'Bold'],[56,'Bold']];
const textStyles={};
for(const [size,weight]of styleData){const name='LaptopStore/'+size+'/'+weight;const s=styles[name]||figma.createTextStyle();s.name=name;s.fontName={family:'Be Vietnam Pro',style:weight};s.fontSize=size;s.lineHeight={unit:'PERCENT',value:size>=30?125:150};styles[s.name]=s;textStyles[s.name]=s.id;}
const screens={};
const defs=[['start','00 · Bắt đầu prototype',-1640,0,960],['home','01 · User / Khám phá laptop',0,0,1320],['detail','02 · User / Chi tiết sản phẩm',1640,0,1080],['cart','03 · User / Giỏ hàng',3280,0,1080],['checkout','04 · User / Thanh toán',4920,0,1080],['success','05 · User / Đặt hàng thành công',6560,0,1080],['staff','06 · Staff / Đơn hàng',0,1800,1080],['staffDetail','07 · Staff / Xử lý đơn hàng',1640,1800,1080],['staffConfirmed','08 · Staff / Đã xác nhận đơn',3280,1800,1080],['inventory','09 · Staff / Tồn kho',4920,1800,1080],['admin','10 · Admin / Tổng quan',0,3400,1080],['products','11 · Admin / Sản phẩm',1640,3400,1080],['productEdit','12 · Admin / Chỉnh sửa sản phẩm',3280,3400,1080],['users','13 · Admin / Khách hàng & nhân sự',4920,3400,1080]];
for(const[k,name,x,y,h]of defs){const f=auto(page,name,'VERTICAL',1440,h,k==='start'?'orangePale':'canvas',0);f.x=x;f.y=y;f.clipsContent=true;f.counterAxisAlignItems='CENTER';f.placeholder=true;screens[k]=f.id;}
const lib=auto(page,'Library · Components & original vector assets','VERTICAL',1440,null,'white',32,48);lib.x=0;lib.y=5000;text(lib,'LaptopStore / UI & assets',30,'Bold');text(lib,'Trắng thoáng. Cam nổi bật. Một hệ thống cho ba vai trò.',14,'Regular','muted');
return {pageId:page.id,libraryId:lib.id,screens,variables,textStyles,collections:[primitives.id,semantic.id,geometry.id],createdNodeIds:ids,scope:'14 desktop frames: role launcher, 5 customer, 4 staff, 4 admin; reusable components; original SVG assets; clickable journeys',discovery:{codeConnect:'none',existingScreens:'empty file',productFont:'no explicit source font; Be Vietnam Pro chosen for redesign',libraryDecision:'SDS not compatible with custom orange semantic model and requested original assets; build local component system'},font:'Be Vietnam Pro'};
