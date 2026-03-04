const API_BASE = 'http://localhost:8080/api';

// --- API calls ---

async function apiGet(path) {
    const res = await fetch(`${API_BASE}${path}`);
    return res.json();
}

async function apiPost(path, body = {}) {
    const res = await fetch(`${API_BASE}${path}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    });
    return res.json();
}

// --- Device registration ---

document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const nodeId = document.getElementById('reg-node').value;
    const type = document.getElementById('reg-type').value;
    const name = document.getElementById('reg-name').value;
    const port = document.getElementById('reg-port').value;

    await apiPost(`/devices/register?node_id=${nodeId}&device_type=${type}&name=${encodeURIComponent(name)}&port=${port}`);
    document.getElementById('register-form').reset();
    refreshDevices();
});

// --- Device rendering ---

function createDeviceCard(device) {
    const card = document.createElement('div');
    card.className = `device-card card ${device.type}`;

    const controls = getControlsForType(device);

    card.innerHTML = `
        <div class="device-header">
            <span class="device-name">${device.name}</span>
            <span class="device-type">${getTypeLabel(device.type)} (nodo ${device.node_id})</span>
        </div>
        <div class="device-state" id="state-${device.node_id}">
            ${formatState(device.state)}
        </div>
        <div class="device-controls">
            ${controls}
        </div>
    `;

    return card;
}

function getTypeLabel(type) {
    const labels = {
        lighting: 'Bombilla',
        lock: 'Cerradura',
        thermostat: 'Termostato',
        window: 'Persiana',
        smoke: 'Sensor humo',
        temperature: 'Sensor temp.',
    };
    return labels[type] || type;
}

function formatState(state) {
    if (!state || Object.keys(state).length === 0) return 'Sin estado';
    return `<pre>${JSON.stringify(state, null, 2)}</pre>`;
}

function getControlsForType(device) {
    const nid = device.node_id;
    switch (device.type) {
        case 'lighting':
            return `
                <button class="primary" onclick="sendCmd(${nid}, 'on')">Encender</button>
                <button onclick="sendCmd(${nid}, 'off')">Apagar</button>
                <label style="width:100%">Brillo
                    <input type="range" min="0" max="254" value="127"
                           onchange="sendCmd(${nid}, 'brightness', [this.value])">
                </label>
            `;
        case 'lock':
            return `
                <button class="danger" onclick="sendCmd(${nid}, 'unlock')">Abrir</button>
                <button class="primary" onclick="sendCmd(${nid}, 'lock')">Cerrar</button>
            `;
        case 'thermostat':
            return `
                <button onclick="sendCmd(${nid}, 'read')">Leer</button>
                <label>Setpoint (x100)
                    <input type="number" id="therm-${nid}" value="2200" style="width:80px">
                </label>
                <button class="primary" onclick="sendCmd(${nid}, 'set', [document.getElementById('therm-${nid}').value])">Ajustar</button>
            `;
        case 'window':
            return `
                <label style="width:100%">Apertura (%)
                    <input type="range" min="0" max="100" value="50"
                           onchange="sendCmd(${nid}, 'set', [this.value])">
                </label>
            `;
        case 'smoke':
            return `
                <button class="danger" onclick="triggerEvent(${nid}, 'smoke-alarm')">Disparar alarma</button>
            `;
        case 'temperature':
            return `
                <button onclick="sendCmd(${nid}, 'temperature')">Leer temperatura</button>
            `;
        default:
            return '';
    }
}

// --- Commands ---

async function sendCmd(nodeId, command, args = []) {
    const result = await apiPost(`/devices/${nodeId}/command`, {
        cluster: '',
        command: command,
        args: args.map(String),
    });
    addEventLog(`Comando: ${command} → nodo ${nodeId}`, JSON.stringify(result.result));
    refreshDevices();
}

async function triggerEvent(nodeId, eventType) {
    const result = await apiPost(`/devices/${nodeId}/trigger`, {
        event_type: eventType,
    });
    addEventLog(`Evento: ${eventType} → nodo ${nodeId}`, JSON.stringify(result.result), true);
    refreshDevices();
}

// --- Event log ---

function addEventLog(message, detail, isAlert = false) {
    const log = document.getElementById('event-log');
    if (log.querySelector('.empty-state')) log.innerHTML = '';

    const item = document.createElement('div');
    item.className = `event-item ${isAlert ? 'alert' : 'info'}`;
    item.innerHTML = `
        <span>${message} <small>${detail}</small></span>
        <span class="event-time">${new Date().toLocaleTimeString()}</span>
    `;
    log.prepend(item);
}

// --- Refresh ---

async function refreshDevices() {
    try {
        const data = await apiGet('/devices');
        const grid = document.getElementById('devices-grid');

        if (data.devices.length === 0) {
            grid.innerHTML = '<p class="empty-state">No hay dispositivos registrados.</p>';
            return;
        }

        grid.innerHTML = '';
        data.devices.forEach(device => {
            grid.appendChild(createDeviceCard(device));
        });
    } catch (err) {
        console.error('Error refreshing devices:', err);
    }
}

// Poll every 5 seconds
setInterval(refreshDevices, 5000);
refreshDevices();
