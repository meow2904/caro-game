let zoomId = null;
let userId = null;
let role = null;
let stompClient = null;

//Setup board canvas
const canvas = document.getElementById("board");
const startBtn = document.getElementById("startBtn");
const ctx = canvas && canvas.getContext('2d', { alpha: false });
const cellSize = 100;
const boardSize = 20; // 20x20
const board = Array(boardSize * boardSize).fill(null);

let devicePixelRatio_ = window.devicePixelRatio || 1;

// Board state
let scale = 1;           // zoom multiplier
let offsetX = 0, offsetY = 0; // panning offset in pixels (board space)
let isPanning = false;
let pointerStart = null;
let panStart = null;
let movedDuringDrag = false;
let turn = 'X';
let moves = new Map();
let history = []; // for undo

// Game state
let isGameReady = false;

// ---------------- MAIN SCRIPT ------------------
(function init() {
    const urlParts = window.location.pathname.split('/');
    const zoomFromURL = urlParts[urlParts.length - 1];

    zoomId = sessionStorage.getItem("zoomId") || zoomFromURL;
    userId = sessionStorage.getItem("userId");
    role = sessionStorage.getItem("role");

    document.getElementById("zoomIdSpan").textContent = zoomId || "Chưa có";

    if (role === "PLAYER_1") {
        const s = document.getElementById("startBtn");
        if (s) s.classList.remove("d-none");
    }

    // Check if user has joined
    if (!role || !userId) {
        // User hasn't joined -> show join form, don't initialize canvas
        showJoinForm();
    } else {
        // User has joined -> show game board and initialize
        showGameBoard();
    }
})();

// ---------------- UI STATE MANAGEMENT ------------------
function showJoinForm() {
    const joinBox = document.getElementById("joinBox");
    const canvasWrap = document.getElementById("canvasWrap");

    // Show Bootstrap join modal
    const joinModalEl = document.getElementById('joinModal');
    if (joinModalEl) {
        try {
            const modal = (window.bootstrap && window.bootstrap.Modal)
                ? (window.bootstrap.Modal.getInstance(joinModalEl) || new window.bootstrap.Modal(joinModalEl))
                : null;
            if (modal && typeof modal.show === 'function') modal.show();
        } catch (e) {
            console.warn('Bootstrap modal show failed', e);
            // fallback: ensure an element exists for older code
            joinModalEl.classList.remove('d-none');
        }
    }

    // Update role display
    document.getElementById("roleSpan").textContent = "Unknown";
    document.getElementById("playerName").textContent = "Not joined";

    // Attach join button handler to Bootstrap modal button
    const joinBtnEl = document.getElementById('joinBtn');
    if (joinBtnEl) joinBtnEl.onclick = joinRoom;

    console.log("Showing join modal - Canvas not initialized");
}

function showGameBoard() {
    const canvasWrap = document.getElementById("canvasWrap");

    // Hide join modal if present
    const joinModalEl = document.getElementById('joinModal');
    if (joinModalEl) {
        try {
            const modal = (window.bootstrap && window.bootstrap.Modal)
                ? (window.bootstrap.Modal.getInstance(joinModalEl) || new window.bootstrap.Modal(joinModalEl))
                : null;
            if (modal && typeof modal.hide === 'function') modal.hide();
        } catch (e) {
            console.warn('Failed to hide join modal', e);
        }
    }
    if (canvasWrap) canvasWrap.classList.remove("disabled");

    // Update role display
    const roleText = role === "PLAYER_1" ? "X" : "O";
    const playerName = sessionStorage.getItem("playerName");

    document.getElementById("roleSpan").textContent = roleText;
    document.getElementById("playerName").textContent = playerName;

    // Initialize game
    initializeGame();

    console.log("Game board shown - Canvas initialized");
}

// ---------------- GAME INITIALIZATION ------------------
function initializeGame() {
    if (isGameReady) {
        console.log("Game already initialized");
        return;
    }

    isGameReady = true;
    console.log("Initializing game...");

    // Setup canvas
    resizeCanvas();
    window.addEventListener('resize', resizeCanvas);

    // Setup event listeners
    setupCanvasEvents();

    // Center board on screen
    centerBoard();

    // Draw initial grid
    draw();

    // Connect to WebSocket
    connectWS();
}

function setupCanvasEvents() {
    if (!canvas) return;
    // Mouse events for panning and placing moves
    canvas.addEventListener('mousedown', handleMouseDown);
    canvas.addEventListener('contextmenu', (ev) => ev.preventDefault());
    canvas.addEventListener('wheel', handleWheel, { passive: false });

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);
}

function setupStartModal() {
    if (!startBtn) return;
    canvas.addEventListener('mousedown', handleMouseDown);
    canvas.addEventListener('contextmenu', (ev) => ev.preventDefault());
    canvas.addEventListener('wheel', handleWheel, { passive: false });

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);
}

// ---------------- CANVAS MANAGEMENT ------------------
function resizeCanvas() {
    if (!canvas || !isGameReady) return;

    devicePixelRatio_ = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    canvas.width = Math.floor(rect.width * devicePixelRatio_);
    canvas.height = Math.floor(rect.height * devicePixelRatio_);
    ctx.setTransform(devicePixelRatio_, 0, 0, devicePixelRatio_, 0, 0);
    draw();
}

function centerBoard() {
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    offsetX = rect.width / 2 - (0.5 * cellSize * scale);
    offsetY = rect.height / 2 - (0.5 * cellSize * scale);
}

// ---------------- COORDINATE UTILITIES ------------------
function toBoardCoords(screenX, screenY) {
    const rect = canvas.getBoundingClientRect();
    const x = (screenX - rect.left) - offsetX;
    const y = (screenY - rect.top) - offsetY;
    const cell = cellSize * scale;

    // Use straightforward floor division. Math.floor works for negative coordinates
    // (e.g. x/cell = -0.2 -> Math.floor -> -1) which is the intended cell index.
    const gx = Math.floor(x / cell);
    const gy = Math.floor(y / cell);

    return { gx, gy };
}

function toCellCenter(gx, gy) {
    const cell = cellSize * scale;
    const rect = canvas.getBoundingClientRect();
    const cx = rect.left + offsetX + (gx + 0.5) * cell;
    const cy = rect.top + offsetY + (gy + 0.5) * cell;
    return { cx, cy };
}


// ---------------- DRAWING FUNCTIONS ------------------
function draw() {
    if (!canvas || !isGameReady) {
        console.log("Skipping draw - game not ready");
        return;
    }

    const w = canvas.width / devicePixelRatio_;
    const h = canvas.height / devicePixelRatio_;

    // Clear canvas with white background
    ctx.fillStyle = '#fff';
    ctx.fillRect(0, 0, w, h);

    const cell = cellSize * scale;
    const cols = Math.ceil(w / cell) + 4;
    const rows = Math.ceil(h / cell) + 4;

    // Compute start indices for visible area
    const startX = Math.floor((-offsetX) / cell) - 2;
    const startY = Math.floor((-offsetY) / cell) - 2;

    // Draw grid lines
    ctx.strokeStyle = '#e0e0e0';
    ctx.lineWidth = Math.max(1, 1 * scale);
    ctx.beginPath();

    // Vertical lines
    for (let i = 0; i <= cols; i++) {
        const x = offsetX + (startX + i) * cell;
        ctx.moveTo(x, 0);
        ctx.lineTo(x, h);
    }

    // Horizontal lines
    for (let j = 0; j <= rows; j++) {
        const y = offsetY + (startY + j) * cell;
        ctx.moveTo(0, y);
        ctx.lineTo(w, y);
    }
    ctx.stroke();

    // Draw all moves
    for (const [key, symbol] of moves) {
        const [gx, gy] = key.split(',').map(Number);
        const cx = offsetX + (gx + 0.5) * cell;
        const cy = offsetY + (gy + 0.5) * cell;

        // Skip if outside visible area
        if (cx < -cell || cy < -cell || cx > w + cell || cy > h + cell) continue;

        if (symbol === 'X') drawX(cx, cy, cell);
        else if (symbol === 'O') drawO(cx, cy, cell);
    }
}

function drawX(cx, cy, cell) {
    const r = Math.min(cell * 0.35, 30);
    ctx.strokeStyle = '#e74c3c';
    ctx.lineWidth = Math.max(2, 3 * scale);
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(cx - r, cy - r);
    ctx.lineTo(cx + r, cy + r);
    ctx.moveTo(cx + r, cy - r);
    ctx.lineTo(cx - r, cy + r);
    ctx.stroke();
}

function drawO(cx, cy, cell) {
    const r = Math.min(cell * 0.35, 30);
    ctx.strokeStyle = '#2c82c9';
    ctx.lineWidth = Math.max(2, 3 * scale);
    ctx.beginPath();
    ctx.arc(cx, cy, r, 0, Math.PI * 2);
    ctx.stroke();
}

// ---------------- INPUT HANDLING ------------------
function handleMouseDown(ev) {
    if (!isGameReady) return;

    ev.preventDefault();
    isPanning = true;
    pointerStart = { x: ev.clientX, y: ev.clientY };
    panStart = { x: offsetX, y: offsetY };
    movedDuringDrag = false;
    canvas.style.cursor = 'grabbing';
}

function handleMouseMove(ev) {
    if (!isPanning || !isGameReady) return;

    const dx = ev.clientX - pointerStart.x;
    const dy = ev.clientY - pointerStart.y;

    // Check if user actually moved
    if (Math.hypot(dx, dy) > 4) movedDuringDrag = true;

    // Update pan offset
    offsetX = panStart.x + dx;
    offsetY = panStart.y + dy;
    draw();
}

function handleMouseUp(ev) {
    if (!isPanning || !isGameReady) return;

    isPanning = false;
    canvas.style.cursor = 'grab';

    // If it was a click (no significant movement), place a move
    if (!movedDuringDrag) {
        const { gx, gy } = toBoardCoords(ev.clientX, ev.clientY);
        // Debug: log computed coordinates to help diagnose mapping issues
        // console.log('Click -> client:', { x: ev.clientX, y: ev.clientY }, 'rect-offset:', { offsetX, offsetY }, '-> board cell:', { gx, gy });
        placeMove(gx, gy);
    }
}

function handleWheel(ev) {
    if (!isGameReady) return;

    ev.preventDefault();

    // Determine zoom direction
    const delta = -ev.deltaY;
    const zoomFactor = delta > 0 ? 1.12 : 0.9;
    const oldScale = scale;
    const newScale = Math.min(10, Math.max(0.3, scale * zoomFactor));

    // Get mouse position relative to canvas
    const rect = canvas.getBoundingClientRect();
    const mx = ev.clientX - rect.left;
    const my = ev.clientY - rect.top;

    // Adjust offset to zoom towards mouse position
    offsetX = mx - (mx - offsetX) * (newScale / oldScale);
    offsetY = my - (my - offsetY) * (newScale / oldScale);
    scale = newScale;

    draw();
}

function placeMove(gx, gy) {
    const key = gx + ',' + gy;

    // Check if cell is already occupied
    if (moves.has(key)) {
        console.log("Cell already occupied:", key);
        return;
    }

    // Send move to server via WebSocket
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/move", {}, JSON.stringify({
            zoomId,
            userId,
            x: gx,
            y: gy
        }));
        console.log("Move sent to server:", { gx, gy });
    } else {
        console.error("WebSocket not connected");
    }
}
// ------------- JOIN ROOM --------------
async function joinRoom() {
    const name = document.getElementById("joinName").value.trim();
    if (!name) return alert("Enter name!");

    const res = await fetch("/api/join", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            zoomId: zoomId,
            playerName: name
        })
    });
    if (!res.ok) {
        //throw new Error(`HTTP error! status: ${res.status}`);
        alert("Join room failed, try again!");
        return;
    }
    const data = await res.json();
    sessionStorage.setItem("playerName", name);
    // console.log("Join successful:", data);

    // Update local variables
    zoomId = data.zoomId;
    userId = data.userId;
    role = data.role;

    // Show game board and initialize
    showGameBoard();
}

// ---------------- GAME UPDATES ------------------
function handleMoveUpdate(payload) {
    const key = payload.x + ',' + payload.y;
    moves.set(key, payload.symbol);
    history.push({ key, symbol: payload.symbol, player: payload.userId });
    //console.log("Move added:", { key, symbol: payload.symbol });

    // Update which player's turn it is now: after someone placed, it's the other player's turn
    try {
        // If the player who placed the move is the current user, turn is opponent's; otherwise it's your turn
        const turnDisplay = (payload.userId === userId) ? 'Opponent' : 'You';
        const turnSpanEl = document.getElementById('turnSpan');
        if (turnSpanEl) turnSpanEl.textContent = turnDisplay;

        // Also update local 'turn' symbol for rendering/logic: flip symbol
        turn = (payload.symbol === 'X') ? 'O' : 'X';
    } catch (e) {
        console.warn('Failed to update turn display', e);
    }

    draw();
}

function handleWin(payload) {
    const winner = payload.winner === userId ? "Bạn" : "Đối thủ";
    setTimeout(() => {
        alert(`${winner} đã thắng!`);
    }, 100);
}


// --------------- WEBSOCKET --------------------
// Connect to WebSocket server and setup subscriptions
function connectWS() {
    const socket = new SockJS(`/ws?zoomId=${zoomId}&userId=${userId}`);
    stompClient = Stomp.over(socket);
    stompClient.debug = () => { };
    stompClient.connect(
        { zoomId, userId },
        frame => {
            console.log("✅ WS Connected:", frame);

            // Subscribe zoom
            stompClient.subscribe(`/topic/room.${zoomId}`, msg => {
                const payload = JSON.parse(msg.body);
                console.log("← WS Message:");

                if (payload.type === "MOVE") {
                    handleMoveUpdate(payload);
                }

                if (payload.type === "ERROR") {
                    const err = JSON.parse(msg.body);
                    console.log("← Error message:", err);
                    alert("❌ ERROR: " + err.message);
                }

                // Handle game start and first player notification
                if (payload.type === "START") {
                    const isHost = (userId === payload.userId);

                    // Notify only the opponent (non-host) who will go first
                    if (!isHost) {
                        const hostGoesFirst = !!payload.firstTurn;
                        const who = hostGoesFirst ? "host" : "you";
                        alert(`Game started! Host set ${who} is First turn`);
                    }

                    // Also update the displayed "turn" (who starts) similar to handleMoveUpdate
                    try {
                        const hostGoesFirst = !!payload.firstTurn;
                        // If host goes first, starter is the host; otherwise it's the other player
                        const starterIsYou = hostGoesFirst ? (userId === payload.userId) : (userId !== payload.userId);
                        const turnSpanEl = document.getElementById('turnSpan');
                        if (turnSpanEl) turnSpanEl.textContent = starterIsYou ? 'You' : 'Opponent';

                        // Update local 'turn' symbol for rendering/logic
                        const mySymbol = (role === "PLAYER_1") ? 'X' : 'O';
                        turn = starterIsYou ? mySymbol : (mySymbol === 'X' ? 'O' : 'X');
                    } catch (e) {
                        console.warn('Failed to set starter display for START payload', e);
                    }

                        // Hide start button for everyone 
                    const startBtnEl = document.getElementById('startBtn');
                    if (startBtnEl) startBtnEl.classList.add('d-none');

                    // Close modal only for the host (creator) side
                    if (isHost) {
                        const startModalEl = document.getElementById('chooseFirstTurnModal');
                        if (startModalEl) {
                            try {
                                const bsModal = (window.bootstrap && window.bootstrap.Modal)
                                    ? (window.bootstrap.Modal.getInstance(startModalEl) || new window.bootstrap.Modal(startModalEl))
                                    : null;

                                if (bsModal && typeof bsModal.hide === 'function') {
                                    bsModal.hide();
                                } else {
                                    startModalEl.classList.remove('show');
                                    startModalEl.style.display = 'none';
                                    startModalEl.hidden = true;
                                    const backdrops = document.querySelectorAll('.modal-backdrop');
                                    backdrops.forEach(b => b.parentNode && b.parentNode.removeChild(b));
                                }
                            } catch (e) {
                                console.warn('Failed to close start modal via Bootstrap API, falling back to hide', e);
                                startModalEl.hidden = true;
                            }
                        }
                    }
                }

                if (payload.type === "WIN") {
                    handleWin(payload);
                }
            });

            // Subscribe user-specific queue
            stompClient.subscribe(`/queue/${userId}/message`, msg => {
                const err = JSON.parse(msg.body);
                console.log("← Error message:", err);
                alert("❌ ERROR: " + err.message);
            });
        },
        error => {
            console.error("❌ WS Connection failed:", error);
            alert("❌ WebSocket connection failed. Please reload the page.");
        }
    );
}

function startGame() {
    const firstTurn = document.querySelector('input[name="firstTurn"]:checked').value === "1";
    if (stompClient && stompClient.connected && role === "PLAYER_1") {
        stompClient.send("/app/start", {}, JSON.stringify({ zoomId, userId, firstTurn }));
    }
}


//change the test function to startGame to the server
function test() {
    const firstTurn = document.querySelector('input[name="firstTurn"]:checked').value === "1";
    if (stompClient && stompClient.connected && role === "PLAYER_1") {
        stompClient.send("/app/test", {}, JSON.stringify({ zoomId, userId, firstTurn }));
    }
}



