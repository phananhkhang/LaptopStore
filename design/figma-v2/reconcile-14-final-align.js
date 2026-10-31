let repairOnly=false;const initialNames=new Set(Object.keys(PV));await ensureVars({'po/confirmLabel':'Xác nhận nhận 10 máy'});
const rx=n=>n.reactions.flatMap(r=>r.actions||(r.action?[r.action]:[]));
const and=(...x)=>x.reduce((a,b)=>({type:'EXPRESSION',resolvedType:'BOOLEAN',value:{expressionFunction:'AND',expressionArguments:[a,b]}}));
const ops=await screen('paymentReconciliationOps'),source=await screen('paymentOnline');function sidebar(n){let p=n;while(p&&!p.name.startsWith('Sidebar/'))p=p.parent;return p?.name;}
for(const n of ops.findAll(n=>n.name.startsWith('Link/'))){const side=sidebar(n),ref=source.findOne(t=>t.name===n.name&&sidebar(t)===side);if(ref?.reactions.length)await act(n,rx(ref));}
const receive=await screen('purchaseReceive'),confirm=actions(receive,'Xác nhận nhận 10 máy')[0];bind(confirm.findOne(n=>n.type==='TEXT'),'po/confirmLabel');
for(const row of (await screen('purchaseOrders')).findAll(n=>n.name.startsWith('Row/'))){const code=row.name.match(/PN-\d+/)?.[0],qty=code==='PN-00043'?15:code==='PN-00044'?20:10;if(['PN-00042','PN-00043','PN-00044'].includes(code))await act(row,[setVar('po/confirmLabel','Xác nhận nhận '+qty+' máy'),...rx(row)]);}
for(const n of actions(await screen('purchaseEdit'),'Lưu phiếu nhập'))await act(n,[setVar('po/confirmLabel','Xác nhận nhận 20 máy'),...rx(n)]);
for(const key of ['purchaseReceive','purchasePartial'])for(const n of (await screen(key)).findAll(n=>n.name==='Action/Xác nhận nhận 10 máy'||n.name==='Action/Nhận 5 máy còn lại'))await act(n,[branch([{condition:and(eq('po/code','PN-00044'),eq('po/additionalSku',true),eq('po/isReceived',false)),actions:[setVar('stock/dellQuantity','28')]}]),...rx(n)]);
return {mutatedNodeIds:[...new Set(changed)],newVariables:Object.fromEntries(Object.entries(PV).filter(([k])=>!initialNames.has(k)).map(([k,v])=>[k,v.id]))};
