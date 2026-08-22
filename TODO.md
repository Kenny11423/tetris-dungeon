# 📋 Tetris Dungeon - Master TODO List

## ⚔️ Battle Mechanics (Cơ chế chiến đấu)
### Debuff on Player (Boss Skills)
- [x] Mù (Blind): Che Next block trong n giây.
- [x] Rơi siêu tốc: Tăng ngẫu nhiên tốc độ block rơi trong n giây.
- [x] Mất bóng: Mất bóng khối gạch (Ghost piece) trong n giây.
- [x] Khóa xoay: Không thể xoay khối trong n khối tiếp (không quá 7 khối).
- [x] Đơn điệu: Chỉ có 1 khối nhất định xuất hiện liên tục.
- [x] Ngược phím: Phím bấm bị inverse trong n giây.
*(n tăng theo độ khó của game từ rất nhỏ)*

### Buff on Boss
- [x] Hồi máu.
- [x] Tăng ATK: Tấn công generate thêm hàng khối (garbage lines) nhiều hơn.
- [x] Haste: Tấn công nhanh hơn (giảm thời gian cooldown).

### Cơ chế (Mechanics)
- [x] 1 boss sẽ có ngẫu nhiên 1 set skill nhỏ dựa theo các hiệu ứng trên. Độ khó tăng lên sẽ thêm skill vào set. Các boss đầu sẽ có timer dùng ngẫu nhiên 1 skill trong set, độ khó càng cao thì timer càng nhanh.
- [x] công/boss tăng cooldown nhẹ khi bị tấn công
- [x] Không xử dụng hệ thống điểm nữa mà đi xa đc bao lâu/ thời gian 1 lần run 


---

## 🎒 Inventory & Equipment System
### Giao diện (UI)
- [x] Thêm khung hiển thị Túi Đồ (Inventory) chứa các vật phẩm đang có.
- [x] Tạo cơ chế chọn và sử dụng vật phẩm bằng phím cứng.
- [x] Sẽ có thêm 3 Ô để đặt thuốc/bomb vào, sẽ có 3 nút bấm để kích hoạt ngay
- [x] Trong inventory sẽ xem được cả chỉ số
- [x] thêm 2 ô cho vũ khí và giáp, khi bấm vào sẽ có 6 ô đặt thèo hình tròn để đặt "đá quý"

### Danh sách vật phẩm
- [x] Thuốc hồi máu (Clear Potion): Sử dụng để xóa một lượng block/hàng rác ở đáy.
- [x] Bomb: gây 1 lượng sát thương nhanh cho quái/boss
- [x] Thuốc tăng chỉ số (Stat Potion): Tăng rõ ràng từng loại stat trong 1 khoảng thời gian/lượt.
- [x] Random Stat Potion: Uống vào nhận buff ngẫu nhiên (hoặc trừ chỉ số, ebuff nếu xui).
- [ ] Thập tự hồi sinh: không thể bấm để sử dụng, đặt vào ô khi chết sẽ được hồi sinh 1 lần (xóa 70% hàng block)
- [x] Thuốc xóa debuff: sử dụng để xóa 1 debuff ( nếu có nhiều debuff thi sẽ xóa cái cũ nhất)

### Hệ thống đá quý trang bị 
- [ ] Thêm các đá quý ở nhưng nơi có thể loot đồ, shop, ...
- [ ] chia rõ đá quý cho vũ khí và giáp riêng biệt
- [ ] đá quý sẽ cho chi số, buf nào đó tùy thuộc vào độ hiếm
- [ ] đá quý có thể có debuff nhưng sẽ có buff tốt hơn để tăng tính "high risk high reward"

### Cơ chế rơi đồ (Drop)
- [x] Thiết lập tỉ lệ rớt vật phẩm khi đánh bại quái/Boss hoặc vượt qua Event.

---

## 📈 Player Stats System
### Chỉ số cơ bản
- [ ] **ATK (Sức mạnh):** Gây thêm sát thương lên Boss mỗi khi ăn hàng.
- [ ] **DEF (Phòng thủ):** Giảm số lượng hàng rác (garbage lines) bị nhận khi Boss tấn công.
- [ ] **LUCK (May mắn):** Tăng tỉ lệ rớt vật phẩm hiếm
- [ ] **EVADE (Né đòn):** Tăng tỉ lệ Né đòn(capped ở một mức độ để không thể lợi dụng kẽ hở)
- [ ] **CRIT CHANCE/CRIT DAMAGE (Chí mạng):** Tăng tỉ lệ và sát thương chí mạng 
- [ ] **DEBUFF RESIST (Kháng hiệu ứng):** Tăng khả năng kháng hiệu ứng xấu 


### Cơ chế nâng cấp
- [ ] Tích hợp nâng cấp chỉ số sau mỗi lần tiêu diệt Boss, từ việc dùng Item, hoặc mua trong Shop/Event.

---

## 🎲 Random Events
### Cơ chế Sự kiện
- [ ] Sau khi vượt qua màn (không phải Boss), hiển thị ngẫu nhiên các Sự kiện thay vì tiếp tục rơi khối luôn.

### Danh sách Sự kiện
- [ ] **Rương Kho Báu:** Mở ra nhận vật phẩm (Thuốc hồi máu, Thuốc chỉ số).
- [ ] **Thương Nhân (Shop):** Bán điểm số (Score) để đổi lấy vật phẩm hoặc nâng cấp Stat.
- [ ] **Thử Thách Ác Quỷ:** Chấp nhận một Debuff (ví dụ mù) trong 1 màn để nhận phần thưởng tương ứng 

---

## 🎮 General & Game Modes
### Điều khiển
- [ ] Controller support: Hỗ trợ tay cầm (Xbox/PlayStation) để chơi game.

### Chế độ chơi (Game Modes)
- [ ] Boss Rush: Chế độ chỉ đánh Boss liên tục. Sau mỗi boss phần thưởng lớn hơn, tỉ lệ đồ hiếm cao hơn. Sau 3 boss sẽ có Shop vào hồi máu, sau đó tăng độ khó.

### Tính năng ẩn
- [ ] Cheat code (Master Control): Sử dụng 1 chuỗi nút bấm đặc biệt để mở khóa bảng điều khiển. Cho phép tự điều chỉnh mức khó, event, chỉ số, item... tùy ý.
