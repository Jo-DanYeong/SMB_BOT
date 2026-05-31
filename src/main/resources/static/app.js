const state = {
    guildId: localStorage.getItem("guildId") || "",
    words: [],
    warnings: [],
    users: [],
    selectedUser: null,
    canManage: false,
};

const el = {
    guildForm: document.querySelector("#guild-form"),
    guildId: document.querySelector("#guild-id"),
    wordForm: document.querySelector("#word-form"),
    wordInput: document.querySelector("#word-input"),
    wordList: document.querySelector("#word-list"),
    wordCount: document.querySelector("#word-count"),
    warnCount: document.querySelector("#warn-count"),
    muteCount: document.querySelector("#mute-count"),
    userForm: document.querySelector("#user-form"),
    userFilter: document.querySelector("#user-id"),
    userList: document.querySelector("#user-search-results"),
    selectedUser: document.querySelector("#selected-user"),
    addWarn: document.querySelector("#add-warn"),
    removeWarn: document.querySelector("#remove-warn"),
    toggleMute: document.querySelector("#toggle-mute"),
    resetWarn: document.querySelector("#reset-warn"),
    warnList: document.querySelector("#warn-list"),
    toast: document.querySelector("#toast"),
    logout: document.querySelector("#logout"),
};

el.guildId.value = state.guildId;

function api(path, options = {}) {
    if (!state.guildId) {
        throw new Error("서버 ID를 먼저 입력하세요.");
    }

    return fetch(`/api/guilds/${encodeURIComponent(state.guildId)}${path}`, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {}),
        },
        ...options,
    }).then(async (response) => {
        if (!response.ok) {
            if (response.status === 403) {
                throw new Error("서버 관리 권한이 필요합니다.");
            }
            if (response.status === 401) {
                window.location.href = "/login.html";
                return null;
            }
            const message = await response.text();
            throw new Error(message || `HTTP ${response.status}`);
        }
        if (response.status === 204) {
            return null;
        }
        return response.json();
    });
}

function showToast(message) {
    el.toast.textContent = message;
    el.toast.classList.add("show");
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => el.toast.classList.remove("show"), 2400);
}

function renderWords() {
    el.wordCount.textContent = state.words.length;
    if (state.words.length === 0) {
        el.wordList.innerHTML = `<div class="empty-state">등록된 금칙어가 없습니다.</div>`;
        return;
    }

    el.wordList.innerHTML = state.words
        .map((item) => `
            <div class="chip">
                <span>${escapeHtml(item.word)}</span>
                ${state.canManage ? `<button type="button" data-word="${escapeAttr(item.word)}" aria-label="삭제">x</button>` : ""}
            </div>
        `)
        .join("");
}

function renderWarnings() {
    setManagerControls();
    el.warnCount.textContent = state.warnings.length;
    el.muteCount.textContent = state.warnings.filter((item) => item.mute).length;

    if (!state.canManage) {
        el.warnList.innerHTML = `<div class="empty-state">서버 관리 권한이 없어 금칙어 목록만 볼 수 있습니다.</div>`;
        return;
    }

    if (state.warnings.length === 0) {
        el.warnList.innerHTML = `<div class="empty-state">경고 기록이 없습니다.</div>`;
        return;
    }

    el.warnList.innerHTML = state.warnings
        .map((item) => `
            <div class="row">
                <span>${escapeHtml(item.displayName || item.userId)}</span>
                <span class="badge">경고 ${item.warncnt}</span>
                <span class="badge ${item.mute ? "on" : ""}">${item.mute ? "뮤트" : "정상"}</span>
            </div>
        `)
        .join("");
}

function renderSelectedUser() {
    if (!state.canManage) {
        el.selectedUser.className = "selected-user empty";
        el.selectedUser.textContent = "서버 관리 권한이 있는 사용자만 유저를 관리할 수 있습니다.";
        return;
    }

    if (!state.selectedUser) {
        el.selectedUser.className = "selected-user empty";
        el.selectedUser.textContent = "목록에서 유저를 선택하면 경고와 뮤트를 관리할 수 있습니다.";
        return;
    }

    const user = state.selectedUser;
    el.selectedUser.className = "selected-user";
    el.selectedUser.textContent = `${user.displayName || user.userId} / 경고 ${user.warncnt}회 / ${user.mute ? "뮤트" : "정상"}`;
}

function renderUserList() {
    if (!state.canManage) {
        el.userList.innerHTML = "";
        return;
    }

    const query = el.userFilter.value.trim().toLowerCase();
    const users = state.users.filter((user) => {
        if (!query) {
            return true;
        }
        return user.displayName.toLowerCase().includes(query)
            || user.username.toLowerCase().includes(query)
            || user.userId.includes(query);
    });

    if (users.length === 0) {
        el.userList.innerHTML = `<div class="empty-state">표시할 유저가 없습니다.</div>`;
        return;
    }

    el.userList.innerHTML = users
        .map((user) => `
            <div class="search-result">
                <div>
                    <strong>${escapeHtml(user.displayName)}</strong>
                    <small>@${escapeHtml(user.username)} · ${escapeHtml(user.userId)}</small>
                </div>
                <button type="button" data-user-id="${escapeAttr(user.userId)}">선택</button>
            </div>
        `)
        .join("");
}

async function loadAll() {
    if (!state.guildId) {
        state.canManage = false;
        state.users = [];
        renderWords();
        renderWarnings();
        renderSelectedUser();
        renderUserList();
        return;
    }

    const [permission, words] = await Promise.all([
        api("/permissions/me"),
        api("/words"),
    ]);

    state.canManage = Boolean(permission?.canManage);
    state.words = words || [];
    state.users = state.canManage ? await api("/users") : [];
    state.warnings = state.canManage ? await api("/warnings") : [];
    state.selectedUser = state.canManage ? state.selectedUser : null;

    renderWords();
    renderWarnings();
    renderSelectedUser();
    renderUserList();
}

async function refreshSelectedUser(userId = state.selectedUser?.userId) {
    if (!userId || !state.canManage) {
        return;
    }
    state.selectedUser = await api(`/warnings/${encodeURIComponent(userId)}`);
    renderSelectedUser();
    await loadAll();
}

el.guildForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    state.guildId = el.guildId.value.trim();
    localStorage.setItem("guildId", state.guildId);
    state.selectedUser = null;
    await run(loadAll, "서버 정보를 불러왔습니다.");
});

el.wordForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!state.canManage) {
        showToast("서버 관리 권한이 필요합니다.");
        return;
    }

    const word = el.wordInput.value.trim();
    if (!word) {
        return;
    }
    await run(async () => {
        await api("/words", {
            method: "POST",
            body: JSON.stringify({ word }),
        });
        el.wordInput.value = "";
        await loadAll();
    }, "금칙어를 추가했습니다.");
});

el.wordList.addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-word]");
    if (!button) {
        return;
    }
    const word = button.dataset.word;
    await run(async () => {
        await api(`/words/${encodeURIComponent(word)}`, { method: "DELETE" });
        await loadAll();
    }, "금칙어를 삭제했습니다.");
});

el.userForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!state.canManage) {
        showToast("서버 관리 권한이 필요합니다.");
        return;
    }
    await run(async () => {
        state.users = await api("/users");
        renderUserList();
    }, "유저 목록을 새로고침했습니다.");
});

el.userFilter.addEventListener("input", renderUserList);

el.userList.addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-user-id]");
    if (!button) {
        return;
    }
    await run(() => selectUser(button.dataset.userId), "유저를 선택했습니다.");
});

el.addWarn.addEventListener("click", () => updateSelectedUser("add", "경고를 추가했습니다."));
el.removeWarn.addEventListener("click", () => updateSelectedUser("remove", "경고를 차감했습니다."));
el.toggleMute.addEventListener("click", async () => {
    if (!state.canManage) {
        showToast("서버 관리 권한이 필요합니다.");
        return;
    }
    if (!state.selectedUser) {
        showToast("유저를 먼저 선택하세요.");
        return;
    }
    await run(async () => {
        const muted = !state.selectedUser.mute;
        await api(`/warnings/${encodeURIComponent(state.selectedUser.userId)}/mute`, {
            method: "PATCH",
            body: JSON.stringify({ muted }),
        });
        await refreshSelectedUser();
    }, "뮤트 상태를 변경했습니다.");
});
el.resetWarn.addEventListener("click", async () => {
    if (!state.canManage) {
        showToast("서버 관리 권한이 필요합니다.");
        return;
    }
    if (!state.selectedUser) {
        showToast("유저를 먼저 선택하세요.");
        return;
    }
    await run(async () => {
        await api(`/warnings/${encodeURIComponent(state.selectedUser.userId)}`, { method: "DELETE" });
        await refreshSelectedUser();
    }, "경고를 초기화했습니다.");
});
el.logout.addEventListener("click", async () => {
    await fetch("/api/auth/logout", { method: "POST" });
    window.location.href = "/login.html";
});

async function updateSelectedUser(action, message) {
    if (!state.canManage) {
        showToast("서버 관리 권한이 필요합니다.");
        return;
    }
    if (!state.selectedUser) {
        showToast("유저를 먼저 선택하세요.");
        return;
    }
    await run(async () => {
        state.selectedUser = await api(`/warnings/${encodeURIComponent(state.selectedUser.userId)}/${action}`, {
            method: "POST",
        });
        renderSelectedUser();
        await loadAll();
    }, message);
}

async function selectUser(userId) {
    state.selectedUser = await api(`/warnings/${encodeURIComponent(userId)}`);
    renderSelectedUser();
    await loadAll();
}

function setManagerControls() {
    const disabled = !state.canManage;
    el.wordInput.disabled = disabled;
    el.wordForm.querySelector("button").disabled = disabled;
    el.userFilter.disabled = disabled;
    el.userForm.querySelector("button").disabled = disabled;
    el.addWarn.disabled = disabled;
    el.removeWarn.disabled = disabled;
    el.toggleMute.disabled = disabled;
    el.resetWarn.disabled = disabled;
}

async function run(task, successMessage) {
    try {
        await task();
        if (successMessage) {
            showToast(successMessage);
        }
    } catch (error) {
        showToast(error.message || "요청을 처리하지 못했습니다.");
    }
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeAttr(value) {
    return escapeHtml(value).replaceAll("`", "&#096;");
}

run(loadAll);
