const form = document.querySelector("#login-form");
const userId = document.querySelector("#user-id");
const password = document.querySelector("#password");
const message = document.querySelector("#message");

fetch("/api/auth/status")
    .then((response) => response.json())
    .then((status) => {
        if (status.authenticated) {
            window.location.href = "/";
        }
    });

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    message.textContent = "";

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userId: userId.value.trim(),
                password: password.value,
            }),
        });

        if (!response.ok) {
            throw new Error("UID 또는 비밀번호가 맞지 않습니다.");
        }

        window.location.href = "/";
    } catch (error) {
        message.textContent = error.message || "로그인하지 못했습니다.";
    }
});
