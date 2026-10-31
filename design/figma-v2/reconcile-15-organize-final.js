await figma.setCurrentPageAsync(await figma.getNodeByIdAsync(S.pageId));const page=figma.currentPage,changed=[],groups={customer:[],staff:[],admin:[]};
for(const[key,id]of Object.entries(S.screens)){const r=await figma.getNodeByIdAsync(id);if(!r)continue;if(r.layoutMode!=='NONE'){r.minHeight=1080;r.layoutSizingVertical='HUG';}const shell=r.findOne(n=>n.name==='Application shell');if(shell)for(const side of shell.children.filter(n=>n.name.startsWith('Sidebar/'))){side.minHeight=1080;side.layoutSizingVertical='FILL';changed.push(side.id);}changed.push(r.id);if(key==='start')continue;const role=/Admin\s*\//.test(r.name)?'admin':/Staff\s*\//.test(r.name)?'staff':'customer';groups[role].push({key,node:r});}
const start=await figma.getNodeByIdAsync(S.screens.start);page.appendChild(start);start.x=0;start.y=0;
let y=start.height+240;const sections={},counts={};
for(const[role,sectionId]of [['customer','20:6004'],['staff','20:6005'],['admin','20:6006']]){const sec=await figma.getNodeByIdAsync(sectionId);sec.x=0;sec.y=y;let rowY=80;
 for(let i=0;i<groups[role].length;i+=4){const row=groups[role].slice(i,i+4);let max=0;for(const[j,item]of row.entries()){sec.appendChild(item.node);item.node.x=64+j*1560;item.node.y=rowY;max=Math.max(max,item.node.height);}rowY+=max+160;}
 sec.resizeWithoutConstraints(6248,rowY);sec.name=({'customer':'01 · Khách hàng','staff':'02 · Staff','admin':'03 · Admin'}[role])+' / '+groups[role].length+' màn hình & trạng thái';sections[sec.name]=sec.id;counts[role]=groups[role].length;changed.push(sec.id);y+=sec.height+240;
}
const dialogs=await figma.getNodeByIdAsync('61:9483');dialogs.name='04 · Hộp thoại & xác nhận / '+Object.keys(S.overlays).length+' trạng thái';page.appendChild(dialogs);dialogs.x=6540;dialogs.y=8000;
let rowY=80;const arr=Object.entries(S.overlays);
for(let i=0;i<arr.length;i+=4){let max=0;for(const[j,[key,id]]of arr.slice(i,i+4).entries()){const root=await figma.getNodeByIdAsync(id);dialogs.appendChild(root);root.x=64+j*1560;root.y=rowY;max=Math.max(max,root.height);changed.push(root.id);}rowY+=max+160;}
dialogs.resizeWithoutConstraints(6248,rowY);sections[dialogs.name]=dialogs.id;
page.name='LaptopStore · UI & Prototype / Đã rà soát dự án';
page.flowStartingPoints=[{nodeId:start.id,name:'Bắt đầu · Chọn vai trò'},{nodeId:S.screens.home,name:'Khách hàng · Mua laptop'},{nodeId:S.screens.staff,name:'Staff · Vận hành'},{nodeId:S.screens.admin,name:'Admin · Quản trị'}];
figma.currentPage.selection=[start];figma.viewport.scrollAndZoomIntoView([start]);
return {createdNodeIds:[],mutatedNodeIds:changed,sections,counts,pageName:page.name,screenCount:Object.keys(S.screens).length,dialogCount:arr.length,start:{id:start.id,width:start.width,height:start.height},canvasBottom:y};
