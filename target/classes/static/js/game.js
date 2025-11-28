
async function newGame() {
    const zoomLink = document.getElementById("zoomLink");

    

    const playerName = document.getElementById("playerName").value.trim();

    if (!playerName) {
        alert("Vui lòng nhập tên của bạn!");
        return;
    }

    try {
        // Gọi API tạo phòng
        const response = await fetch("/api/createZoom", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ playerName: playerName })
            
        });
        const data = await response.json();
        const zoomId = data.zoomId;
        const userId = data.userId;
        const role = data.role;
        sessionStorage.setItem("zoomId", zoomId);
        sessionStorage.setItem("userId", userId);
        sessionStorage.setItem("role", role);

        zoomLink.innerHTML = `Link game: <a href="/game/${zoomId}" target="_blank">${zoomId}</a>`;

        // Lưu tên người chơi tạm (nếu muốn)
        sessionStorage.setItem("playerName", playerName);

        // Chuyển sang trang game
        window.location.href = `/game/${zoomId}`;

    } catch (err) {
        console.error(err);
        alert("Tạo phòng thất bại, thử lại!");
    }
}

// Gắn sự kiện nút khi DOM đã load
document.addEventListener("DOMContentLoaded", () => {
    const createBtn = document.getElementById("createGameBtn");
    createBtn.addEventListener("click", newGame);
});
