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
- [x] Thập tự hồi sinh: không thể bấm để sử dụng, đặt vào ô khi chết sẽ được hồi sinh 1 lần (xóa 70% hàng block)
- [x] Thuốc xóa debuff: sử dụng để xóa 1 debuff ( nếu có nhiều debuff thi sẽ xóa cái cũ nhất)

### Hệ thống đá quý trang bị 
- [x] Thêm các đá quý ở nhưng nơi có thể loot đồ, shop, ...
- [x] chia rõ đá quý cho vũ khí và giáp riêng biệt
- [x] đá quý sẽ cho chi số, buf nào đó tùy thuộc vào độ hiếm
- [x] đá quý có thể có debuff nhưng sẽ có buff tốt hơn để tăng tính "high risk high reward"

### Cơ chế rơi đồ (Drop)
- [x] Thiết lập tỉ lệ rớt vật phẩm khi đánh bại quái/Boss hoặc vượt qua Event.

---

## 📈 Player Stats System
### Chỉ số cơ bản
- [x] **ATK (Sức mạnh):** Gây thêm sát thương lên Boss mỗi khi ăn hàng.
- [x] **DEF (Phòng thủ):** Giảm số lượng hàng rác (garbage lines) bị nhận khi Boss tấn công.
- [x] **LUCK (May mắn):** Tăng tỉ lệ rớt vật phẩm hiếm.
- [x] **EVADE (Né đòn):** Tăng tỉ lệ Né đòn.
- [x] **CRIT CHANCE/CRIT DAMAGE (Chí mạng):** Tăng tỉ lệ và sát thương chí mạng.
- [x] **DEBUFF RESIST (Kháng hiệu ứng):** Tăng khả năng kháng hiệu ứng xấu.


### Cơ chế nâng cấp
- [x] Tích hợp nâng cấp chỉ số sau mỗi lần tiêu diệt Boss, từ việc dùng Item, hoặc mua trong Shop/Event.

---

## 🎲 Random Events
### Cơ chế Sự kiện
- [x] Sau khi vượt qua màn (không phải Boss), hiển thị ngẫu nhiên các Sự kiện thay vì tiếp tục rơi khối luôn.

### Danh sách Sự kiện & Phần thưởng
- [x] **Rương Nhặt Rơi Đồ (Victory Drop Chest):** Luôn luôn xuất hiện chắc chắn sau khi tiêu diệt quái/boss để nhặt quà và Vàng.
- [x] **Các Loại Rương Sự Kiện (Event Chest Types):**
  - [x] **Rương Cổ Đại (Ancient Relic Chest):** Mở ra nhận quà cổ vật hiếm & Vàng lớn.
  - [x] **Rương Khóa Hoàng Gia (Locked Royal Vault - 🔑 Needed):** Yêu cầu vật phẩm `Dungeon Key` (Chìa khóa hầm ngục nhặt từ quái/mua trong Shop) để mở ra Kho vàng khổng lồ (500G+), Đá quý hoàn hảo & Thập tự hồi sinh.
  - [x] **Bẫy Rương Mimic (Mimic Chest Trap - 👾 25% Chance):** 25% tỉ lệ Rương sự kiện biến thành Bẫy Mimic cắn người chơi và phạt thêm 6 hàng khối rác lên bàn cờ!
- [x] **Thương Nhân (Shop Event):** Bán các loại Vật phẩm tiêu hao, Chìa khóa & Đá quý ngẫu nhiên theo giá trị bằng Vàng (Gold).
- [x] **Thử Thách Ác Quỷ (Devil's Challenge Event):** Chấp nhận một Debuff (Mù màu, Khối rơi siêu tốc, Tăng hàng rác) ở màn tới để nhận lượng Vàng lớn (250 - 400 Gold) & Vật phẩm/Đá quý hiếm!

---

## 🎮 General & Game Modes
### Điều khiển
- [x] **Controller support 🎮:** Hỗ trợ tay cầm (Xbox / PlayStation / Arcade Gamepad) & Phím tắt mở rộng (WASD, Numpad, Enter, Shift, J/K/L, U/I/O). Đã bổ sung cửa sổ Cài đặt phím bấm `Controller & Settings` tại Menu chính!

### Chế độ chơi (Game Modes)
- [x] **Boss Rush Mode 👑:** Chế độ đánh Boss liên tục từ Stage 5 trở đi. Máu Boss & Tốc độ phạt tăng dần theo Tier. Thưởng x2 Gold & Đá quý hiếm. Cứ sau 3 Boss tiêu diệt được Goddess chữa lành bàn cờ (xóa 7 hàng) và mở đồng thời Shop + Đền Chỉ Số!

### Tính năng ẩn
- [x] **Cheat code (Master Control):** Bấm phím phím tắt bí mật `F12` hoặc `Ctrl + Shift + C` để mở Bảng điều khiển Master Control Console. Cho phép cộng Vàng, Max Stats, Hồi phục bàn cờ, Nhảy màn Stage, Spawn đồ và Kích hoạt Sự kiện lập tức!
