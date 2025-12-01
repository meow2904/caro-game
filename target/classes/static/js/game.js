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

        document.getElementById("zoomLinkText").innerHTML = `
          <div class="d-flex align-items-center gap-2">
            <span>🎮 ZoomId: <strong>${zoomId}</strong></span>
            <button type="button" class="btn btn-sm btn-outline-success" onclick="copyToClipboard('${zoomId}')">📋 Copy</button>
          </div>
        `;
        zoomLink.classList.remove("d-none");

        // Disable buttons
        const createBtn = document.getElementById("createGameBtn");
        const joinBtn = document.getElementById("joinGameBtn");
        createBtn.disabled = true;
        joinBtn.disabled = true;

        // Temporarily save player name (optional)
        sessionStorage.setItem("playerName", playerName);

        // Countdown and redirect
        let countdown = 5;
        const countdownEl = document.createElement("div");
        countdownEl.className = "alert alert-info mt-3 mb-0 text-center";
        countdownEl.id = "countdownText";
        zoomLink.parentNode.insertBefore(countdownEl, zoomLink.nextSibling);

        const countdownInterval = setInterval(() => {
            countdownEl.textContent = `⏳ Entering game in ${countdown} second${countdown !== 1 ? 's' : ''}...`;
            countdown--;
            if (countdown < 0) {
                clearInterval(countdownInterval);
                window.location.href = `/game/${zoomId}`;
            }
        }, 1000);

        countdownEl.textContent = `⏳ Entering game in 3 seconds...`;

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

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        alert("✅ ZoomId copied to clipboard!");
    }).catch(() => {
        alert("❌ Failed to copy ZoomId");
    });
}

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
