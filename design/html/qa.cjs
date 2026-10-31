'use strict';
const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {chromium}=require('C:/Users/ADMIN/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const root=path.resolve(__dirname,'..'),out=path.join(root,'qa');fs.mkdirSync(out,{recursive:true});
const url='file:///'+path.join(root,'preview.html').replace(/\\/g,'/');let browser;
(async()=>{
 browser=await chromium.launch({headless:true,executablePath:'C:/Program Files/BraveSoftware/Brave-Browser/Application/brave.exe'});
 const context=await browser.newContext({viewport:{width:1440,height:1000},reducedMotion:'reduce'}),page=await context.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.goto(url);await page.evaluate(async()=>{await document.fonts.ready;await Promise.all([400,500,600,700].map(w=>document.fonts.load(w+' 16px "Be Vietnam Pro"')));});await page.locator('.hero h1').waitFor();
 const fonts=await page.evaluate(()=>({body:getComputedStyle(document.body).fontFamily,faces:[...document.fonts].map(f=>({family:f.family,weight:f.weight,status:f.status}))}));
 assert(fonts.faces.every(f=>f.status==='loaded'),'Embedded fonts must load');
 const visit=async key=>{await page.evaluate(k=>{location.hash=k;},key);await page.waitForFunction(k=>route===k,key);await page.locator('#main').waitFor();};
 for(const key of ['home','staff','admin']){await visit(key);await page.screenshot({path:path.join(out,key+'-desktop.png'),fullPage:key==='home'});}
 const routes=await page.evaluate(()=>Object.keys(renderers)),layout=[];
 for(const viewport of [{width:1440,height:1000},{width:768,height:1024},{width:390,height:844}]){
  await page.setViewportSize(viewport);
  for(const key of routes){await visit(key);const q=await page.evaluate(()=>({width:document.documentElement.scrollWidth,viewport:innerWidth,main:document.querySelector('#main')?.innerText.trim().length,broken:[...document.images].filter(i=>!i.complete||i.naturalWidth===0).length,unknown:[...document.querySelectorAll('[data-action]')].filter(n=>!actions[n.dataset.action]).map(n=>n.dataset.action)}));layout.push({key,viewport:viewport.width,...q});}
 }
 fs.writeFileSync(path.join(out,'layout.json'),JSON.stringify({errors,layout},null,2));
 await page.setViewportSize({width:1440,height:1000});await visit('home');
 await page.evaluate(()=>{state=initialState();state.coupons[0].perUser=3;save();ui.order='LS24102601';});
 // Actual customer flow: configure, add to cart, edit quantity, coupon, checkout.
 await visit('detail');await page.locator('[data-action="variant"][data-index="1"]').click();assert.match(await page.locator('.detail-price').innerText(),/30\.000\.000/);
 await page.locator('[data-action="add-cart"]').click();assert.equal(await page.evaluate(()=>state.cart.find(i=>i.sku==='ROG-G15-02').qty),1);
 await visit('cart');await page.locator('[data-action="qty"][data-sku="ROG-G15-01"][data-delta="1"]').click();assert.equal(await page.evaluate(()=>state.cart.find(i=>i.sku==='ROG-G15-01').qty),2);
 await page.locator('[data-action="remove-cart"][data-sku="ROG-G15-02"]').click();await page.locator('[data-action="qty"][data-sku="ROG-G15-01"][data-delta="-1"]').click();
 await page.locator('form[data-form="coupon"] input').fill('LAPTOP500');await page.locator('form[data-form="coupon"] button').click();assert.match(await page.locator('[data-testid="cart-total"]').innerText(),/24\.500\.000/);
 await visit('checkout');await page.locator('form[data-form="checkout"] button[type="submit"]').click();await page.waitForFunction(()=>route==='success');
 const orderId=await page.evaluate(()=>ui.order);assert.equal(await page.evaluate(()=>findSku('ROG-G15-01').v.quantity),3);assert.equal(await page.evaluate(()=>state.cart.length),0);
 // Operational lifecycle and collect COD exactly once.
 await visit('staffDetail');for(let i=0;i<4;i++){await page.locator('[data-action="order-next"]').click();await page.locator('#modal [data-action="commit"]').click();}
 assert.equal(await page.evaluate(()=>currentOrder().status),'DELIVERED');await page.locator('[data-action="collect-cod"]').click();await page.locator('#modal [data-action="commit"]').click();await page.waitForFunction(()=>route==='codConfirmed');assert.equal(await page.evaluate(()=>currentOrder().payment),'PAID');
 // Receiving is cumulative: partial then remaining, stock/ledger updated once.
 await visit('purchaseReceive');await page.locator('input[name="receive0"]').fill('4');await page.locator('form[data-form="receive"] button[type="submit"], form[data-form="receive"] .form-actions button').click();await page.locator('#modal [data-action="commit"]').click();await page.waitForFunction(()=>route==='purchaseDone');assert.equal(await page.evaluate(()=>state.purchases[0].status),'PARTIALLY_RECEIVED');
 await visit('purchaseReceive');await page.locator('input[name="receive0"]').fill('6');await page.locator('form[data-form="receive"] .form-actions button').click();await page.locator('#modal [data-action="commit"]').click();await page.waitForFunction(()=>route==='purchaseDone');assert.equal(await page.evaluate(()=>state.purchases[0].status),'RECEIVED');assert.equal(await page.evaluate(()=>findSku('ROG-G15-01').v.quantity),13);
 // Admin stock adjustment and validation.
 await visit('stockAdjust');await page.locator('input[name="delta"]').fill('-2');await page.locator('textarea[name="reason"]').fill('Kiểm kê thực tế tháng 10');await page.locator('form[data-form="adjust"] .form-actions button').click();await page.locator('#modal [data-action="commit"]').click();await page.waitForFunction(()=>route==='stockHistory');assert.equal(await page.evaluate(()=>findSku('ROG-G15-01').v.quantity),11);
 // Profile edit persists; modal confirmation toggles a review.
 await visit('profile');await page.locator('input[name="name"]').fill('Nguyễn Minh An QA');await page.locator('form[data-form="profile"] button').click();assert.equal(await page.evaluate(()=>state.profile.name),'Nguyễn Minh An QA');
 await visit('moderation');await page.locator('[data-action="toggle-review"]').first().click();await page.locator('#modal [data-action="commit"]').click();assert.equal(await page.evaluate(()=>state.reviews[0].visible),false);
 // Filter results, AI recommendation and VNPay failed/retry/success.
 await visit('catalog&clear=1'.split('&')[0]);await page.locator('input[data-filter="filterBrand"][value="Dell"]').check();assert.equal(await page.locator('.catalog-products .product-card').count(),1);
 await visit('ai');await page.locator('input[name="prompt"]').fill('Laptop học tập dưới 20 triệu');await page.locator('form[data-form="chat"] button').click();assert.equal(await page.locator('.recommendation').count(),1);
 await page.evaluate(()=>{ui.order='LS24102601';});await visit('payPending');await page.locator('[data-action="gateway"]').click();await page.locator('[data-action="gateway-result"][data-success="0"]').click();await page.waitForFunction(()=>route==='payFail');await page.locator('[data-action="gateway"]').click();await page.locator('[data-action="gateway-result"][data-success="1"]').click();await page.waitForFunction(()=>route==='paySuccess');assert.equal(await page.evaluate(()=>currentOrder().payment),'PAID');
 // Accessibility/navigation: mobile drawer and modal Escape.
 await page.setViewportSize({width:390,height:844});await visit('staff');await page.getByRole('button',{name:'Mở menu',exact:true}).click();assert(await page.locator('#sidebar').evaluate(n=>n.classList.contains('open')));await page.locator('[data-action="screen-map"]').first().click();await page.keyboard.press('Escape');assert.equal(await page.locator('#modal').evaluate(n=>n.open),false);
 for(const key of ['home','detail','cart','staff','admin']){await visit(key);await page.evaluate(()=>document.querySelector('#toast')?.remove());await page.screenshot({path:path.join(out,key+'-mobile.png'),fullPage:key==='home'});}
 // Restore clean demo for screenshots / final deliverable; persistent data doesn't affect file.
 const overflow=layout.filter(q=>q.width>q.viewport+2),emptyScreens=layout.filter(q=>!q.main),brokenImages=layout.filter(q=>q.broken),unknownActions=layout.filter(q=>q.unknown.length);
 const result={checkedAt:new Date().toISOString(),routes:routes.length,viewports:[1440,768,390],renderChecks:layout.length,fonts,errors,overflow,emptyScreens,brokenImages,unknownActions,flows:['variant/cart/coupon/checkout','order lifecycle/COD','partial+full receiving/ledger','stock adjustment','profile persistence','review moderation','catalog filtering','AI recommendation','VNPay failure/retry/success','mobile navigation/modal Escape'],createdOrder:orderId};
 fs.writeFileSync(path.join(out,'report.json'),JSON.stringify(result,null,2));console.log(JSON.stringify(result,null,2));await browser.close();
 assert.equal(errors.length,0,'No runtime errors');assert.equal(overflow.length,0,'No page horizontal overflow');assert.equal(emptyScreens.length,0,'No empty screens');assert.equal(brokenImages.length,0,'No broken images');assert.equal(unknownActions.length,0,'All buttons have handlers');
})().catch(async e=>{console.error(e);if(browser)await browser.close();process.exitCode=1;});
