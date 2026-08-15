# TETRIS ROGUELIKE — Tài liệu thiết kế game (Game Design Document)

## 1. Tổng quan

**Tên tạm thời:** Tetris Ascension (có thể đổi)

**Thể loại:** Puzzle chiến đấu (Puzzle-Battler) kết hợp Roguelike

**Ý tưởng cốt lõi:** Người chơi xếp gạch Tetris như bình thường, nhưng thay vì chỉ "xóa dòng để lấy điểm", việc xóa dòng sẽ gây **sát thương** lên một con **Boss** có thanh máu (HP bar) đứng đối diện. Hạ gục boss → mở khóa phòng tiếp theo → độ khó tăng dần → chọn nâng cấp (roguelike) → gặp boss mạnh hơn.

**Đối tượng tham khảo:** Tetris Effect, Tetris 99, kết hợp cơ chế roguelike kiểu Slay the Spire / Hades (chọn buff giữa các trận, build đa dạng theo mỗi lượt chơi - "run").

---

## 2. Vòng lặp gameplay chính (Core Gameplay Loop)

```
Vào phòng Boss → Xếp gạch Tetris → Xóa dòng gây sát thương
      → Boss phản công (gây khó/chướng ngại lên bàn chơi)
      → Lặp lại đến khi Boss hết máu hoặc người chơi thua
      → Thắng: nhận thưởng + chọn 1 trong 3 nâng cấp (relic/skill)
      → Sang phòng/tầng tiếp theo, độ khó tăng
      → Sau N boss → Boss trùm (Elite/Final Boss) của chương
```

---

## 3. Cơ chế chiến đấu bằng Tetris

### 3.1. Gây sát thương
| Hành động | Sát thương gợi ý |
|---|---|
| Xóa 1 dòng | 10 dmg |
| Xóa 2 dòng (Double) | 25 dmg |
| Xóa 3 dòng (Triple) | 45 dmg |
| Tetris (xóa 4 dòng) | 100 dmg + hiệu ứng đặc biệt |
| T-Spin | x1.5 dmg, hồi thêm năng lượng skill |
| Combo liên tiếp (Back-to-back) | +% dmg cộng dồn theo streak |

### 3.2. Cơ chế phòng thủ / rủi ro — Hệ thống Timer tấn công của Boss

**Bố cục màn hình:** chia 2 bên — bên phải là bàn chơi Tetris của người chơi (gameplay chính), bên trái là hình ảnh/animation Boss + thanh máu + thanh Timer tấn công. Boss **không có bàn chơi riêng**, chỉ tấn công theo nhịp thời gian.

**a) Thanh Timer (Attack Gauge)**
- Hiển thị dạng thanh đếm ngược hoặc vòng tròn xoay cạnh Boss.
- Khi timer về 0 → Boss thực hiện 1 đòn tấn công → timer reset cho chu kỳ kế tiếp.
- Thời gian mỗi chu kỳ **giảm dần theo độ khó**: Boss 1 ≈ 15s/đòn, Boss 5 ≈ 8s/đòn (tinh chỉnh qua playtest).

**b) Người chơi "ngắt nhịp" Boss bằng cách xóa dòng**
- Mỗi lần xóa dòng sẽ **cộng thêm thời gian vào timer Boss** (làm chậm nhịp tấn công):
  - Xóa 1 dòng: +1s
  - Double: +2s
  - Triple: +3s
  - Tetris: +4-5s
  - T-Spin: +thêm hệ số x1.5
- Tạo thế đánh đổi chiến lược: chơi nhanh/hiệu quả vừa gây dmg lên máu Boss, vừa tự vệ bằng cách trì hoãn đòn đánh tới.

**c) Telegraph — báo hiệu trước đòn đánh (bắt buộc để đảm bảo công bằng)**
- Khi timer còn khoảng 20-30% cuối chu kỳ → Boss vào trạng thái "chuẩn bị": rung hình, phát sáng đỏ, âm thanh cảnh báo.
- Người chơi có 2-3s để phản ứng: dọn dòng gấp, dùng skill phòng thủ, hoặc chấp nhận ăn đòn.

**d) Phân loại đòn tấn công theo timer**
| Loại đòn | Điều kiện | Hiệu ứng |
|---|---|---|
| Đòn thường | Timer về 0 (thông thường) | Đổ 1-3 hàng garbage vào đáy bàn chơi |
| Đòn nặng (có telegraph dài hơn) | Sau mỗi 2-3 chu kỳ timer | Sát thương/garbage nhiều hơn, hoặc kích hoạt gimmick riêng của Boss |
| Đòn Enrage | Trận kéo dài quá lâu (quá N chu kỳ) | Rút ngắn vĩnh viễn thời lượng chu kỳ timer, tấn công dồn dập hơn — chống việc người chơi "câu giờ" |

**e) Gắn gimmick riêng vào timer của từng Boss** (ví dụ)
- *Quỷ Rác:* mỗi lần timer về 0, đổ garbage với lỗ hổng **vị trí ngẫu nhiên** (không cố định 1 cột) → buộc người chơi linh hoạt thay vì học thuộc pattern.
- *Titan Thời Gian:* mỗi lần timer về 0, không đổ garbage mà **rút ngắn luôn chu kỳ timer tiếp theo 10%** → áp lực dồn dập tăng dần, đúng chất "tăng tốc thời gian".

**f) Thua trận**
- Người chơi có **thanh máu (HP)** riêng, giảm khi ăn đòn từ Boss (ngoài garbage line, một số đòn có thể trừ máu trực tiếp).
- Nếu bàn chơi tràn (top-out) hoặc HP về 0 → thua, kết thúc run (đúng tinh thần roguelike: chết là mất run, phải chơi lại từ đầu).

### 3.3. Kỹ năng chủ động (Active Skills)
Tích lũy năng lượng (mana/ultimate gauge) từ việc xóa dòng, dùng để kích hoạt kỹ năng đặc biệt, ví dụ:
- **Búa đập:** phá hủy 1 hàng rác ngay lập tức.
- **Đóng băng thời gian:** làm chậm tốc độ rơi khối trong 5s.
- **Song Trùng:** dòng tiếp theo xóa được tính damage x2.

---

## 4. Hệ thống Boss

### 4.1. Thiết kế Boss
Mỗi boss có:
- **Thanh máu (HP)** hiển thị rõ trên màn hình.
- **Cơ chế đặc trưng (gimmick)** riêng biệt — không chỉ là "máu nhiều hơn":
  - *Boss 1 - Rùa Đá:* Tốc độ rơi khối bình thường, không gimmick — dạy người chơi cơ bản.
  - *Boss 2 - Quỷ Rác:* Định kỳ đổ thêm garbage line vào đáy bàn chơi.
  - *Boss 3 - Bóng Tối:* Che một phần bàn chơi (fog of war), chỉ thấy khối đang rơi.
  - *Boss 4 - Song Sinh:* Hai thanh máu, đòn tấn công gây hiệu ứng đảo ngược phím điều khiển tạm thời.
  - *Boss 5 - Titan Thời Gian:* Tăng tốc độ rơi liên tục theo thời gian trận đấu.
- **Enrage timer:** nếu trận kéo quá lâu, boss vào trạng thái giận dữ (tốc độ rơi nhanh hơn, sát thương phản đòn cao hơn) — tránh người chơi "câu giờ" farm an toàn.

### 4.2. Đường cong độ khó (Difficulty Curve)

| Yếu tố | Tăng theo mỗi Boss |
|---|---|
| Máu Boss | +15–25% mỗi tầng |
| Tốc độ rơi khối (gravity) | Tăng dần theo cấp độ Boss |
| Tần suất phản công / đổ garbage | Tăng dần |
| Số gimmick kết hợp cùng lúc | Boss sau có thể cộng dồn 2 gimmick của boss trước |
| Thời gian enrage | Rút ngắn dần |

Gợi ý công thức đơn giản cho HP boss:
```
HP(n) = HP_base * (1.2 ^ (n-1))
```
với `n` là số thứ tự boss, `HP_base` là máu boss đầu tiên. Có thể tinh chỉnh bằng playtest thực tế thay vì chỉ dựa vào công thức.

### 4.3. Cấu trúc theo chương (Run Structure)
- Mỗi **Run** gồm 3 chương (Act), mỗi chương ~5 boss thường + 1 Elite Boss cuối chương.
- Giữa các boss: **phòng sự kiện** (chọn nâng cấp, cửa hàng đổi tài nguyên, phòng nghỉ hồi máu).
- Sau chương 3: **Boss cuối (Final Boss)** — tổng hợp toàn bộ gimmick đã gặp.

---

## 5. Yếu tố Roguelike

### 5.1. Meta-progression (giữa các run)
- Mở khóa nhân vật/khối Tetris đặc biệt, relic mới, khó độ mới sau mỗi lần chơi (dù thắng hay thua).
- Hệ thống tiền tệ vĩnh viễn (currency) dùng để mở khóa nội dung mới cho các lần chơi sau.

### 5.2. Trong 1 run (run-based build)
- **Relic (Di vật):** hiệu ứng thụ động, ví dụ: "Mỗi Tetris hồi 5 HP", "Xóa dòng dưới 3s tính x2 dmg".
- **Thẻ nâng cấp khối (Piece Mod):** thay đổi hình dạng/tỷ lệ xuất hiện của các khối (ví dụ tăng tỷ lệ khối I để dễ Tetris).
- **Lựa chọn không đối xứng:** sau mỗi boss, cho chọn 1 trong 3 nâng cấp ngẫu nhiên → tạo build đa dạng mỗi lần chơi, tăng khả năng chơi lại (replayability).
- **Rủi ro – phần thưởng:** phòng "Boss tinh anh" tự nguyện, khó hơn nhưng phần thưởng tốt hơn.

---

## 6. Giao diện & Trải nghiệm người chơi (UI/UX)

**Bố cục tổng thể: chia 2 bên màn hình**

| Bên trái — Boss | Bên phải — Người chơi |
|---|---|
| Hình ảnh/animation Boss | Bàn chơi Tetris tiêu chuẩn (10x20) |
| Thanh máu (HP bar) Boss, đổi màu/rung khi trúng đòn nặng | Next piece, Hold piece |
| Thanh Timer tấn công (đếm ngược/vòng xoay), chuyển đỏ + rung khi sắp tấn công (telegraph) | Combo counter, Skill gauge, HP người chơi |

- Boss không có bàn chơi riêng — mọi tương tác của Boss thể hiện qua animation + thanh Timer, giữ trọng tâm gameplay ở bàn chơi bên phải.
- Khi người chơi xóa dòng, có hiệu ứng trực quan nối từ bàn chơi sang thanh Timer bên trái (VD: tia sáng bắn từ dòng vừa xóa vào thanh timer) để người chơi thấy rõ hành động của mình đang "làm chậm" Boss.
- Telegraph đòn đánh cần rõ ràng: đổi màu thanh Timer (xanh → vàng → đỏ nhấp nháy) kết hợp âm thanh cảnh báo tăng dần.
- Feedback trực quan mạnh khi Tetris/T-Spin (rung màn hình, flash, âm thanh đặc trưng) để tăng cảm giác "đánh boss đã tay".

---

## 7. Công nghệ & phạm vi phát triển đề xuất

| Giai đoạn | Nội dung |
|---|---|
| Prototype (2–4 tuần) | Core Tetris engine + 1 boss cơ bản + dmg system, kiểm chứng gameplay có "vui" không |
| Vertical Slice (4–6 tuần) | 3 boss đầy đủ gimmick, hệ thống relic cơ bản, 1 chương hoàn chỉnh |
| Alpha | Đủ 3 chương, meta-progression, cân bằng độ khó qua playtest |
| Beta | Polish UI/UX, âm thanh, hiệu ứng, cân bằng lại dựa trên phản hồi người chơi |

**Gợi ý công cụ:** Godot hoặc Unity (2D) cho tốc độ phát triển nhanh; có thể làm bản web (HTML5/Canvas hoặc Phaser.js) nếu muốn thử nghiệm nhanh và dễ chia sẻ.

---

## 8. Rủi ro thiết kế cần lưu ý

- **Cân bằng khó/dễ:** nếu tốc độ rơi tăng quá nhanh, người chơi mất khả năng tư duy đặt khối chiến thuật — cần playtest kỹ đường cong độ khó.
- **Lặp lại nhàm chán:** cần đảm bảo mỗi boss có gimmick đủ khác biệt, tránh cảm giác "boss sau chỉ là máu nhiều hơn".
- **RNG khối Tetris:** cần cân nhắc dùng "7-bag randomizer" (chuẩn Tetris hiện đại) để tránh người chơi bị "đói" một loại khối quan trọng khi cần combo.

---

*Tài liệu này là bản kế hoạch khởi điểm — có thể mở rộng thêm phần lore/cốt truyện, thiết kế nhân vật chơi được, hệ thống âm nhạc thích ứng (adaptive music) theo diễn biến trận đấu nếu cần.*
