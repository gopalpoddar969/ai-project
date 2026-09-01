// ============================================================
// Commerce AI Assistant - Chat UI
// ============================================================
//
// General:
//   GET /api/ai/ask
//
// Product:
//   GET /api/ai/semantic-search
//
// Conversation memory:
//   A UUID is generated automatically for each browser tab.
//   It is stored internally in sessionStorage.
//   It is NOT added to the URL.
//   It is NOT displayed in the UI.
//   It is sent only through the X-Conversation-Id header.
// ============================================================

/**
 * Configures the available chat tabs and their backend endpoints.
 */
const TABS = [
    {
        name: 'general',
        endpoint: '/api/ai/ask'
    },
    {
        name: 'product',
        endpoint: '/api/ai/semantic-search'
    }
];

// ============================================================
// Current active tab
// ============================================================

let activeTab = 'general';

// ============================================================
// Conversation ID
// ============================================================
//
// Each browser tab gets its own UUID.
//
// sessionStorage is intentionally used instead of localStorage.
//
// Therefore:
//
//   Browser Tab A -> Conversation UUID A
//   Browser Tab B -> Conversation UUID B
//
// Reloading the same tab keeps its conversation.
//
// Closing the tab removes the sessionStorage entry.
// ============================================================

/**
 * Returns the conversation identifier associated with a chat tab.
 *
 * @param {string} tabName name of the chat tab
 * @returns {string} conversation identifier
 */
function getConversationId(tabName) {
    const storageKey = 'commerce-ai-conversation-' + tabName;

    let conversationId = sessionStorage.getItem(storageKey);

    if (!conversationId) {
        conversationId = crypto.randomUUID();
        sessionStorage.setItem(storageKey, conversationId);
    }

    return conversationId;
}

// ============================================================
// HTML escaping
// ============================================================

/**
 * Escapes HTML-sensitive characters in text.
 *
 * @param {string} str text to escape
 * @returns {string} escaped text
 */
function escapeHtml(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// ============================================================
// Small markdown renderer
// ============================================================

/**
 * Converts the supported Markdown formatting into HTML.
 *
 * @param {string} text text containing Markdown
 * @returns {string} rendered HTML
 */
function renderMarkdown(text) {
    let html = escapeHtml(text);

    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

    html = html.replace(/`([^`]+)`/g, '<code>$1</code>');

    return html;
}

// ============================================================
// Append message
// ============================================================

/**
 * Appends a message to the selected chat element.
 *
 * @param {HTMLElement} chatEl chat container
 * @param {string} role message role
 * @param {string} text message text
 */
function appendMessage(chatEl, role, text) {
    const div = document.createElement('div');

    div.className = 'message message-' + role;

    if (role === 'ai') {
        div.innerHTML = renderMarkdown(text);
    } else {
        div.textContent = text;
    }

    chatEl.appendChild(div);

    chatEl.scrollTop = chatEl.scrollHeight;
}

// ============================================================
// Typing indicator
// ============================================================

/**
 * Displays the typing indicator for the specified chat tab.
 *
 * @param {HTMLElement} chatEl chat container
 * @param {string} tabName name of the chat tab
 */
function showTypingIndicator(chatEl, tabName) {
    const el = document.createElement('div');

    el.className = 'typing';

    el.id = 'typing-' + tabName;

    el.innerHTML = '<span></span><span></span><span></span>';

    chatEl.appendChild(el);

    chatEl.scrollTop = chatEl.scrollHeight;
}

/**
 * Removes the typing indicator for the specified chat tab.
 *
 * @param {string} tabName name of the chat tab
 */
function removeTypingIndicator(tabName) {
    const el = document.getElementById('typing-' + tabName);

    if (el) {
        el.remove();
    }
}

// ============================================================
// Error handling
// ============================================================

/**
 * Displays an error message.
 *
 * @param {HTMLElement} errorEl error container
 * @param {string} message error message
 */
function showError(errorEl, message) {
    errorEl.textContent = message;
    errorEl.hidden = false;
}

/**
 * Clears the displayed error message.
 *
 * @param {HTMLElement} errorEl error container
 */
function clearError(errorEl) {
    errorEl.textContent = '';
    errorEl.hidden = true;
}

// ============================================================
// API call
// ============================================================

/**
 * Sends a question to the backend API for the selected tab.
 *
 * @param {Object} tab tab configuration
 * @param {string} question user question
 * @returns {Promise<string>} backend response text
 */
async function askApi(tab, question) {
    const conversationId = getConversationId(tab.name);

    const url = tab.endpoint + '?question=' + encodeURIComponent(question);

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'X-Conversation-Id': conversationId
        }
    });

    if (!response.ok) {
        throw new Error('Request failed with status ' + response.status);
    }

    /*
     * General endpoint returns String.
     *
     * Semantic-search endpoint returns JSON.
     */
    if (tab.name === 'product') {
        const data = await response.json();

        /*
         * The backend returns:
         *
         * {
         *   answer: "...",
         *   ...
         * }
         *
         * Only display the answer in the chat bubble.
         */
        if (data && data.answer) {
            return data.answer;
        }

        return JSON.stringify(data);
    }

    return await response.text();
}

// ============================================================
// Setup one tab
// ============================================================

/**
 * Configures event handling for a chat tab.
 *
 * @param {Object} tab tab configuration
 */
function setupTab(tab) {
    const chatEl = document.getElementById('chat-' + tab.name);
    const formEl = document.getElementById('form-' + tab.name);
    const inputEl = document.getElementById('input-' + tab.name);
    const errorEl = document.getElementById('error-' + tab.name);
    const sendBtn = formEl.querySelector('button[type="submit"]');

    /**
     * Handles submission of a chat question.
     *
     * @param {SubmitEvent} event form submission event
     */
    async function handleSubmit(event) {
        event.preventDefault();

        const question = inputEl.value.trim();

        if (!question) {
            return;
        }

        clearError(errorEl);

        appendMessage(chatEl, 'user', question);

        inputEl.value = '';

        sendBtn.disabled = true;

        inputEl.disabled = true;

        showTypingIndicator(chatEl, tab.name);

        try {
            const answer = await askApi(tab, question);

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

    /**
     * Handles keyboard input for sending questions or creating new lines.
     *
     * @param {KeyboardEvent} event keyboard event
     */
    function handleKeyDown(event) {
        /*
         * Enter -> send
         *
         * Shift + Enter -> newline
         */
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();

            formEl.requestSubmit();
        }
    }

    formEl.addEventListener('submit', handleSubmit);

    inputEl.addEventListener('keydown', handleKeyDown);
}

// ============================================================
// Tab switching
// ============================================================

/**
 * Switches the visible chat tab and focuses its input.
 *
 * @param {string} tabName name of the tab to activate
 */
function switchTab(tabName) {
    activeTab = tabName;

    document
        .querySelectorAll('.tab')
        .forEach(btn => {
            const isActive = btn.dataset.tab === tabName;

            btn.classList.toggle('active', isActive);

            btn.setAttribute('aria-selected', isActive ? 'true' : 'false');
        });

    document
        .querySelectorAll('.tab-panel')
        .forEach(panel => {
            panel.hidden = panel.dataset.panel !== tabName;
        });

    const activeInput = document.getElementById('input-' + tabName);

    if (activeInput) {
        activeInput.focus();
    }
}

// ============================================================
// Clear active tab
// ============================================================

/**
 * Clears the active chat and its corresponding server-side conversation.
 */
function clearActiveTab() {
    const chatEl = document.getElementById('chat-' + activeTab);
    const errorEl = document.getElementById('error-' + activeTab);
    const inputEl = document.getElementById('input-' + activeTab);

    /*
     * Clear browser UI.
     */
    chatEl.innerHTML = '';

    const msg = activeTab === 'general'
        ? 'Chat cleared. Ask a new general question below.'
        : 'Chat cleared. Ask a new product question below.';

    appendMessage(chatEl, 'system', msg);

    clearError(errorEl);

    /*
     * Clear the corresponding Redis conversation.
     *
     * We do this through the backend.
     */
    clearConversationOnServer(activeTab);

    inputEl.focus();
}

// ============================================================
// Clear Redis conversation
// ============================================================

/**
 * Clears the server-side Redis conversation for the selected tab.
 *
 * @param {string} tabName name of the chat tab
 * @returns {Promise<void>} promise completed after the request
 */
async function clearConversationOnServer(tabName) {
    try {
        const conversationId = getConversationId(tabName);

        await fetch('/api/ai/conversation', {
            method: 'DELETE',
            headers: {
                'X-Conversation-Id': conversationId
            }
        });

    } catch (error) {
        console.error('Unable to clear server conversation:', error);
    }
}

// ============================================================
// Initialisation
// ============================================================

TABS.forEach(setupTab);

document
    .querySelectorAll('.tab')
    .forEach(btn => {
        btn.addEventListener('click', () => switchTab(btn.dataset.tab));
    });

document.getElementById('clearBtn').addEventListener('click', clearActiveTab);

window.addEventListener('DOMContentLoaded', () => {
    document.getElementById('input-general').focus();
});