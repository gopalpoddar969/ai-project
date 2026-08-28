// Simple two-tab chat UI for the Commerce AI Assistant.
// Talks to the existing Spring Boot endpoints:
//   GET /api/ai/ask?question=...      (General tab)
//   GET /api/ai/product?question=...  (Product tab)

// One config entry per tab. Same logic; just a different endpoint.
const TABS = [
    { name: 'general', endpoint: '/api/ai/ask' },
    { name: 'product', endpoint: '/api/ai/rag' }
];

// Keeps track of which tab the user is currently viewing.
let activeTab = 'general';

// Escape HTML so untrusted text (like the AI response) can't inject markup.
function escapeHtml(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// Very small markdown -> HTML pass. Handles the formatting the AI actually emits:
//   **bold**            -> <strong>bold</strong>
//   `inline code`       -> <code>inline code</code>
// Everything else stays as escaped plain text. `white-space: pre-wrap`
// in the CSS already preserves the AI's line breaks and indentation.
function renderMarkdown(text) {
    let html = escapeHtml(text);
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
    return html;
}

function appendMessage(chatEl, role, text) {
    const div = document.createElement('div');
    div.className = 'message message-' + role; // user | ai | system
    if (role === 'ai') {
        // Only the AI's replies are rendered as (very simple) markdown.
        // User/system text stays as plain text.
        div.innerHTML = renderMarkdown(text);
    } else {
        div.textContent = text;
    }
    chatEl.appendChild(div);
    chatEl.scrollTop = chatEl.scrollHeight;
}

function showTypingIndicator(chatEl, tabName) {
    const el = document.createElement('div');
    el.className = 'typing';
    el.id = 'typing-' + tabName;
    el.innerHTML = '<span></span><span></span><span></span>';
    chatEl.appendChild(el);
    chatEl.scrollTop = chatEl.scrollHeight;
}

function removeTypingIndicator(tabName) {
    const el = document.getElementById('typing-' + tabName);
    if (el) el.remove();
}

function showError(errorEl, message) {
    errorEl.textContent = message;
    errorEl.hidden = false;
}

function clearError(errorEl) {
    errorEl.textContent = '';
    errorEl.hidden = true;
}

async function askApi(endpoint, question) {
    const url = endpoint + '?question=' + encodeURIComponent(question);
    const response = await fetch(url, { method: 'GET' });
    if (!response.ok) {
        throw new Error('Request failed with status ' + response.status);
    }
    // The backend returns a plain String.
    return await response.text();
}

// Wire up a single tab (chat area + form + error banner) to its endpoint.
function setupTab(tab) {
    const chatEl   = document.getElementById('chat-'  + tab.name);
    const formEl   = document.getElementById('form-'  + tab.name);
    const inputEl  = document.getElementById('input-' + tab.name);
    const errorEl  = document.getElementById('error-' + tab.name);
    const sendBtn  = formEl.querySelector('button[type="submit"]');

    async function handleSubmit(event) {
        event.preventDefault();
        const question = inputEl.value.trim();
        if (!question) return;

        clearError(errorEl);
        appendMessage(chatEl, 'user', question);
        inputEl.value = '';
        sendBtn.disabled = true;
        inputEl.disabled = true;
        showTypingIndicator(chatEl, tab.name);

        try {
            const answer = await askApi(tab.endpoint, question);
            removeTypingIndicator(tab.name);
            appendMessage(chatEl, 'ai', answer);
        } catch (err) {
            removeTypingIndicator(tab.name);
            showError(errorEl, 'Sorry, something went wrong: ' + err.message);
            console.error(err);
        } finally {
            sendBtn.disabled = false;
            inputEl.disabled = false;
            inputEl.focus();
        }
    }

    function handleKeyDown(event) {
        // Enter submits; Shift+Enter adds a newline.
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            formEl.requestSubmit();
        }
    }

    formEl.addEventListener('submit', handleSubmit);
    inputEl.addEventListener('keydown', handleKeyDown);
}

// Switches which tab is visible.
function switchTab(tabName) {
    activeTab = tabName;

    document.querySelectorAll('.tab').forEach(btn => {
        const isActive = btn.dataset.tab === tabName;
        btn.classList.toggle('active', isActive);
        btn.setAttribute('aria-selected', isActive ? 'true' : 'false');
    });

    document.querySelectorAll('.tab-panel').forEach(panel => {
        panel.hidden = panel.dataset.panel !== tabName;
    });

    const activeInput = document.getElementById('input-' + tabName);
    if (activeInput) activeInput.focus();
}

// Clears only the active tab's chat and error state.
function clearActiveTab() {
    const chatEl  = document.getElementById('chat-'  + activeTab);
    const errorEl = document.getElementById('error-' + activeTab);
    const inputEl = document.getElementById('input-' + activeTab);

    chatEl.innerHTML = '';
    const msg = activeTab === 'general'
        ? 'Chat cleared. Ask a new general question below.'
        : 'Chat cleared. Ask a new product question below.';
    appendMessage(chatEl, 'system', msg);
    clearError(errorEl);
    inputEl.focus();
}

// --- Initialisation ---
TABS.forEach(setupTab);

document.querySelectorAll('.tab').forEach(btn => {
    btn.addEventListener('click', () => switchTab(btn.dataset.tab));
});

document.getElementById('clearBtn').addEventListener('click', clearActiveTab);

window.addEventListener('DOMContentLoaded', () => {
    document.getElementById('input-general').focus();
});
