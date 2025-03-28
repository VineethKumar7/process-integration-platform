// Operator console for the Process Integration Platform.
// Submits a JSON request to /api/approvals and polls the status endpoint
// until the BPMN process either ends or hits the manager-review user task.

const form = document.getElementById("approvalForm");
const output = document.getElementById("output");
const submit = form.querySelector("button[type=submit]");

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    submit.disabled = true;
    output.hidden = false;
    output.textContent = "Submitting…";

    const payload = Object.fromEntries(new FormData(form).entries());
    payload.amount = parseFloat(payload.amount);

    try {
        const r = await fetch("/api/approvals", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        const j = await r.json();
        if (!r.ok) throw new Error(j.message || `HTTP ${r.status}`);
        output.textContent = `Started process ${j.processInstanceId}\nPolling status…`;
        await pollStatus(j.processInstanceId);
    } catch (err) {
        output.textContent = `Error: ${err.message}`;
    } finally {
        submit.disabled = false;
    }
});

async function pollStatus(pid) {
    for (let i = 0; i < 10; i++) {
        await new Promise(r => setTimeout(r, 600));
        const s = await fetch(`/api/approvals/${pid}`).then(r => r.json());
        output.textContent = JSON.stringify(s, null, 2);
        if (s.ended) return;
    }
    output.textContent += "\n(stopped polling — process is waiting at a user task; open Camunda Cockpit at /camunda)";
}
