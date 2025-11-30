async function newGame() {
    const zoomLink = document.getElementById("zoomLink");
    const playerName = document.getElementById("playerName").value.trim();
    if (!playerName) {
        alert("Please enter your name!");
        document.getElementById("playerName").focus();
        return;
    }

    try {
        // Call API to create room
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

        document.getElementById("zoomLinkText").innerHTML = `🎮 ZoomId: <a href="/game/${zoomId}" target="_blank" class="alert-link">${zoomId}</a>`;
        zoomLink.classList.remove("d-none");

        // Temporarily save player name (optional)
        sessionStorage.setItem("playerName", playerName);

        // Redirect to game page after a short delay
        setTimeout(() => {
            window.location.href = `/game/${zoomId}`;
        }, 4000);

    } catch (err) {
        console.error(err);
        alert("Tạo phòng thất bại, vui lòng thử lại!");
    }
}

// Attach button event when DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
    const createBtn = document.getElementById("createGameBtn");
    const joinBtn = document.getElementById("joinGameBtn");
    
    createBtn.addEventListener("click", newGame);
    joinBtn.addEventListener("click", joinGame);
});

async function joinGame() {
    const zoomId = document.getElementById("zoomIdInput").value.trim();
    if (!zoomId) {
        alert("Please enter Zoom ID!");
        document.getElementById("zoomIdInput").focus();
        return;
    }

    try {
        // Redirect directly to game page with zoomId
        window.location.href = `/game/${zoomId}`;
    } catch (err) {
        console.error(err);
        alert("Lỗi khi join game, vui lòng thử lại!");
    }
}
