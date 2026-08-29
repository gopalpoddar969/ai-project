const TABS = [
    { name: 'general', endpoint: '/api/ai/ask' },
    { name: 'product', endpoint: '/api/ai/semantic-search' }
];

let activeTab = 'general';

function escapeHtml(str) {
    if (str === null || str === undefined) {
        return '';
    }

    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function renderMarkdown(text) {
    if (text === null || text === undefined) {
        return '';
    }

    let html = escapeHtml(text);
    const codeBlocks = [];

    html = html.replace(/`([^`]+)`/g, function (match, code) {
        const index = codeBlocks.length;
        codeBlocks.push('<code>' + code + '</code>');
        return '___CODE_BLOCK_' + index + '___';
    });

    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

    const lines = html.split('\n');
    let result = [];
    let tableRows = [];
    let insideTable = false;

    function isTableRow(line) {
        const trimmed = line.trim();
        return trimmed.startsWith('|') && trimmed.endsWith('|');
    }

    function isTableSeparator(line) {
        const trimmed = line.trim();

        if (!isTableRow(trimmed)) {
            return false;
        }

        const cells = trimmed.slice(1, -1).split('|').map(cell => cell.trim());
        return cells.length > 0 && cells.every(cell => /^:?-+:?$/.test(cell));
    }

    function parseTableRow(line) {
        return line.trim().slice(1, -1).split('|').map(cell => cell.trim());
    }

    function flushTable() {
        if (!insideTable || tableRows.length === 0) {
            return;
        }

        let tableHtml = '<table class="ai-table">';
        const header = tableRows[0];

        tableHtml += '<thead><tr>';

        header.forEach(cell => {
            tableHtml += '<th>' + cell + '</th>';
        });

        tableHtml += '</tr></thead>';

        if (tableRows.length > 1) {
            tableHtml += '<tbody>';

            for (let i = 1; i < tableRows.length; i++) {
                tableHtml += '<tr>';

                tableRows[i].forEach(cell => {
                    tableHtml += '<td>' + cell + '</td>';
                });

                tableHtml += '</tr>';
            }

            tableHtml += '</tbody>';
        }

        tableHtml += '</table>';
        result.push(tableHtml);

        tableRows = [];
        insideTable = false;
    }

    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];

        if (isTableRow(line)) {
            if (isTableSeparator(line)) {
                insideTable = true;
                continue;
            }

            if (!insideTable) {
                insideTable = true;
            }

            tableRows.push(parseTableRow(line));
            continue;
        }

        flushTable();

        if (line.trim() === '') {
            result.push('<br>');
        } else {
            result.push(line + '<br>');
        }
    }

    flushTable();

    html = result.join('');

    codeBlocks.forEach((codeHtml, index) => {
        html = html.replace('___CODE_BLOCK_' + index + '___', codeHtml);
    });

    return html;
}

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

    if (el) {
        el.remove();
    }
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

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Accept': 'application/json, text/plain, */*'
        }
    });

    if (!response.ok) {
        throw new Error('Request failed with status ' + response.status);
    }

    const contentType = response.headers.get('content-type') || '';
    const responseText = await response.text();

    if (contentType.includes('application/json')) {
        try {
            const json = JSON.parse(responseText);

            if (json && typeof json === 'object' && Object.prototype.hasOwnProperty.call(json, 'answer')) {
                return json.answer || 'No answer was returned.';
            }

            return responseText;
        } catch (error) {
            console.warn('Response was marked as JSON but could not be parsed.', error);
            return responseText;
        }
    }

    return responseText;
}

function setupTab(tab) {
    const chatEl = document.getElementById('chat-' + tab.name);
    const formEl = document.getElementById('form-' + tab.name);
    const inputEl = document.getElementById('input-' + tab.name);
    const errorEl = document.getElementById('error-' + tab.name);
    const sendBtn = formEl.querySelector('button[type="submit"]');

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
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            formEl.requestSubmit();
        }
    }

    formEl.addEventListener('submit', handleSubmit);
    inputEl.addEventListener('keydown', handleKeyDown);
}

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

    if (activeInput) {
        activeInput.focus();
    }
}

function clearActiveTab() {
    const chatEl = document.getElementById('chat-' + activeTab);
    const errorEl = document.getElementById('error-' + activeTab);
    const inputEl = document.getElementById('input-' + activeTab);

    chatEl.innerHTML = '';

    const message = activeTab === 'general'
        ? 'Chat cleared. Ask a new general question below.'
        : 'Chat cleared. Ask a new product question below.';

    appendMessage(chatEl, 'system', message);
    clearError(errorEl);
    inputEl.focus();
}

TABS.forEach(setupTab);

document.querySelectorAll('.tab').forEach(btn => {
    btn.addEventListener('click', () => switchTab(btn.dataset.tab));
});

document.getElementById('clearBtn').addEventListener('click', clearActiveTab);

window.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('input-general');

    if (input) {
        input.focus();
    }
});