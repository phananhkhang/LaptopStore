// Additional states keep the principal happy paths complete.
for(const [i,key,label,next]of [[0,'staffProcessing','Đang chuẩn bị','Bàn giao vận chuyển'],[1,'staffShipping','Đang giao','Xác nhận đã giao'],[2,'staffDelivered','Đã giao','Xác nhận thu COD']]){
 const source=await screen('staffConfirmed');const n=source.clone();n.name='54 · Staff / '+label;n.x=i*1640;n.y=16400;S.screens[key]=n.id;ids.push(n.id,...n.findAll(()=>true).map(c=>c.id));
 for(const q of n.findAllWithCriteria({types:['INSTANCE']})){if(q.name==='Action/Chuẩn bị hàng'){props(q,{Label:next});q.name='Action/'+next;}else if(q.name.startsWith('Badge/')&&Object.values(q.componentProperties).some(p=>p.type==='TEXT'&&p.value==='Đã xác nhận'))props(q,{Label:label});}
 for(const t of n.findAllWithCriteria({types:['TEXT']})){if(t.characters==='Đã xác nhận đơn hàng. Bắt đầu chuẩn bị hàng cho khách.')t.characters='Đã cập nhật trạng thái: '+label+'.';if(t.characters==='○  Đang chuẩn bị')t.characters='●  Đang chuẩn bị';if(i>=1&&t.characters==='○  Đang giao')t.characters='●  Đang giao';if(i>=2&&t.characters==='○  Đã giao')t.characters='●  Đã giao';}
}
{
const source=await screen('success');const n=source.clone();n.name='57 · User / Thanh toán thành công';n.x=4920;n.y=16400;S.screens.paySuccess=n.id;ids.push(n.id,...n.findAll(()=>true).map(c=>c.id));for(const q of n.findAllWithCriteria({types:['INSTANCE']})){if(q.name.startsWith('Badge/'))props(q,{Label:'THANH TOÁN THÀNH CÔNG'});}for(const t of n.findAllWithCriteria({types:['TEXT']})){if(t.characters==='Thanh toán khi nhận hàng')t.characters='Đã thanh toán qua VNPay';if(t.characters==='Chiếc laptop mới sắp đến rồi!')t.characters='Thanh toán thành công!';}
}
// Resolve every interactive control by its purpose and its containing screen.
const direct={
 'Khám phá cửa hàng':'home','Mở khu vực staff':'staffOverview','Mở khu vực admin':'admin',
 'Trang chủ':'home','Cửa hàng':'home','Quay về cửa hàng':'home','Tiếp tục mua sắm':'home','Khám phá ngay':'catalog','Tất cả laptop':'catalog','Xem tất cả laptop':'catalog','Laptop':'catalog',
 'Khuyến mãi':'catalog','Tư vấn AI':'ai','Tư vấn cùng AI':'ai','Tài khoản':'profile','Giỏ hàng':'cart','Sản phẩm':'products',
 'Mua ngay':'checkout','Thêm vào giỏ':'cart','Xem chi tiết':'detail','Xem':'detail','Áp dụng':'cart','Tiến hành thanh toán':'checkout','Quay lại giỏ hàng':'cart','Đặt hàng':'success',
 'Theo dõi đơn hàng':'tracking','Đơn hàng của tôi':'orders','Đơn hàng':'orders','Viết đánh giá':'review','Gửi đánh giá':'orders','Quay lại đơn hàng':'orders',
 'Đăng nhập':'profile','Đăng ký':'profile','Đăng xuất':'login','Hồ sơ':'profile','Sổ địa chỉ':'addresses','Đổi mật khẩu':'password','Thêm địa chỉ':'addressEdit','Sửa':'addressEdit','Chỉnh sửa':'addressEdit','Lưu hồ sơ':'profile','Lưu địa chỉ':'addresses','Quay lại sổ địa chỉ':'addresses','Quay lại hồ sơ':'profile',
 'Thanh toán ngay':'payPending','Thanh toán lại':'payPending','Chuyển đến VNPay':'paySuccess','VNPay':'payPending','COD':'checkout','Xem chi tiết đơn hàng':'tracking',
 'Tạo phiếu nhập':'purchaseEdit','Lưu phiếu nhập':'purchaseOrders','Nhận hàng':'purchaseReceive','Xác nhận nhận 10 máy':'purchaseDone','Xem lịch sử kho':'stockHistory','Lịch sử':'stockHistory','Về danh sách phiếu nhập':'purchaseOrders','Về danh sách thanh toán':'payments','Xác nhận đã thu COD':'codConfirmed',
 'Ẩn đánh giá':'moderationHidden','Hiển thị lại đánh giá':'moderation','Về danh sách đánh giá':'moderation',
 'Thêm sản phẩm':'productEdit','Lưu sản phẩm':'products','Quay lại sản phẩm':'products','Thay hình ảnh':'productEdit','Thêm biến thể':'productEdit',
 'Thêm danh mục':'categories','Lưu danh mục':'categories','Thêm thương hiệu':'brands','Lưu thương hiệu':'brands',
 'Tạo mã giảm giá':'couponEdit','Thêm mã giảm giá':'couponEdit','Lưu mã giảm giá':'couponList','Lượt sử dụng':'couponUsage','Xem lượt sử dụng':'couponUsage',
 'Thêm nhà cung cấp':'supplierEdit','Lưu nhà cung cấp':'suppliers','Lưu vai trò':'users','Lưu thay đổi':'users',
 'Điều chỉnh tồn':'stockAdjust','Xác nhận điều chỉnh':'inventory','Kiểm tra tồn kho':'inventory','Xuất báo cáo':'report','Chọn vai trò':'start',
 'Tất cả':'catalog','Gaming':'catalog','Học tập & văn phòng':'catalog','Mỏng nhẹ':'catalog','Đồ họa':'catalog','Áp dụng bộ lọc':'catalog','Xóa bộ lọc':'catalog','Tìm kiếm lại':'catalog','Xóa sản phẩm':'emptyCart','Xem đánh giá':'review','Gửi tin nhắn':'ai',
 'Đổi địa chỉ':'addresses','Sửa địa chỉ':'addressEdit','Xóa địa chỉ':'addresses','Đặt làm mặc định':'addresses','Đăng nhập tài khoản khác':'login','Cuộc trò chuyện mới':'ai','Máy mỏng nhẹ để đi làm':'ai',
 'Tồn kho thấp':'inventory','Tất cả danh mục':'products','Đang kinh doanh':'products','Tất cả vai trò':'users','Đang hoạt động':'users','Đang chuẩn bị':'orders','Đang giao':'orders','Đã giao':'orders',
 'Mở thanh toán':'payments','Tất cả đơn hàng':'staff','Xem tất cả đơn':'staff','Xem tồn kho':'inventory','Hủy tạo phiếu':'purchaseOrders','Xem chi tiết đơn':'staffDetail','Quay lại thanh toán':'payments','Quay lại khuyến mãi':'couponList','Lịch sử sử dụng':'couponUsage','Tạm ngưng mã':'couponList','Xem sản phẩm':'products','Bàn giao vận chuyển':'staffShipping','Xác nhận đã giao':'staffDelivered','Xác nhận thu COD':'paymentDetail'
};
const navStaff={'Tổng quan':'staffOverview','Đơn hàng':'staff','Kho hàng':'inventory','Nhập hàng':'purchaseOrders','Nhà cung cấp':'staffSuppliers','Thanh toán':'payments','Đánh giá':'moderation','Chọn vai trò':'start'};
const navAdmin={'Tổng quan':'admin','Sản phẩm':'products','Danh mục':'categories','Thương hiệu':'brands','Đơn hàng':'staff','Kho hàng':'inventory','Khách hàng & nhân sự':'users','Khuyến mãi':'couponList','Nhà cung cấp':'suppliers','Báo cáo':'report','Chọn vai trò':'start'};
const keyById=Object.fromEntries(Object.entries(S.screens).map(([k,v])=>[v,k]));
const connections=[];const unresolved=[];
for(const[key,id]of Object.entries(S.screens)){
 const root=await figma.getNodeByIdAsync(id);root.overflowDirection='VERTICAL';if(['catalog','orders'].includes(key))root.resize(1440,1280);
 const candidates=root.findAll(n=>n.name.startsWith('Link/')||n.name.startsWith('Action/')||n.name.startsWith('Row/')||n.name==='Search');
 for(const n of candidates){let label=n.name.replace(/^(Link|Action|Row)\//,'').replace(/\s*[→↗]$/,'').trim();let to;
 let sidebar=n.parent;while(sidebar&&sidebar!==root&&!sidebar.name.startsWith('Sidebar/'))sidebar=sidebar.parent;
 if(sidebar&&sidebar.name.startsWith('Sidebar/'))to=(sidebar.name.endsWith('Staff')?navStaff:navAdmin)[label];
 else if(n.name.startsWith('Row/'))to=key==='staff'?'staffDetail':key==='inventory'?'stockHistory':key==='products'?'productEdit':key==='users'?'userDetail':key==='payments'?'paymentDetail':key==='purchaseOrders'?'purchaseReceive':key==='couponList'?'couponEdit':key==='suppliers'?'supplierEdit':key==='moderation'?'moderationHidden':key==='categories'?'categories':key==='brands'?'brands':undefined;
 else if(label.startsWith('Product/')||label==='Xem chi tiết'&&key==='catalog')to='detail';
 else if(label==='Xem chi tiết'&&key==='orders')to='tracking';
 else if(label==='Đăng nhập'&&key==='register')to='login';
 else if(label==='Đăng ký'&&key==='login')to='register';
 else if(label==='Quay lại đơn hàng'&&['staffDetail','staffConfirmed'].includes(key))to='staff';
 else if(label==='Xác nhận đơn')to='staffConfirmed';
 else if(label==='Chuẩn bị hàng')to='staffProcessing';
 else if(label==='Đổi mật khẩu'&&key==='password')to='profile';
 else if(label==='Xem đơn hàng')to='staff';
 else if(label==='Trang trước'||label==='Trang sau'||label==='Hủy chỉnh sửa')to=key;
 else if(label==='Quay lại')to=key==='supplierEdit'?'suppliers':'start';
 else if(n.name==='Search')to='catalog';
 else to=direct[label];
 if(!to){if(/nhận|nhập.*hàng|phiếu nhập/i.test(label))to='purchaseReceive';else if(/hồ sơ/i.test(label))to='profile';else if(/nhà cung cấp/i.test(label))to=key.startsWith('staff')?'staffSuppliers':'suppliers';else if(/thu cod|chi tiết thanh toán|giao dịch/i.test(label))to='paymentDetail';else if(/lượt dùng|lượt sử dụng/i.test(label))to='couponUsage';else if(/tạo.*mã|sửa.*mã/i.test(label))to='couponEdit';else if(/điều chỉnh/i.test(label))to='stockAdjust';else if(/phân quyền|khóa|chi tiết tài khoản/i.test(label))to='userDetail';else if(/sửa|chi tiết|xử lý/i.test(label))to=['staff','staffOverview'].includes(key)?'staffDetail':key==='products'?'productEdit':key==='users'?'userDetail':key==='suppliers'?'supplierEdit':undefined;else if(/giá|trạng thái|thương hiệu|ngày|lọc|bộ sưu tập|laptop|1|2|3/i.test(label))to=['catalog','emptySearch','home'].includes(key)?'catalog':key;}
 if(to&&S.screens[to]){await n.setReactionsAsync([{trigger:{type:'ON_CLICK'},actions:[{type:'NODE',destinationId:S.screens[to],navigation:'NAVIGATE',transition:{type:'DISSOLVE',easing:{type:'EASE_OUT'},duration:.18},resetScrollPosition:true}]}]);connections.push({from:key,nodeId:n.id,label,to});ids.push(n.id);}else unresolved.push({key,nodeId:n.id,label});
 }
 ids.push(root.id);
}
figma.currentPage.flowStartingPoints=[{nodeId:S.screens.start,name:'00 / Chọn trải nghiệm'},{nodeId:S.screens.home,name:'01 / Mua sắm'},{nodeId:S.screens.staffOverview,name:'02 / Vận hành'},{nodeId:S.screens.admin,name:'03 / Quản trị'}];
return {createdNodeIds:ids,mutatedNodeIds:ids,screens:S.screens,connections,unresolved,flowStartingPoints:figma.currentPage.flowStartingPoints};
