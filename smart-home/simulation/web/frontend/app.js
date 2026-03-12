const API_BASE = `${window.location.origin}/api`;

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

async function apiDelete(path) {
    const res = await fetch(`${API_BASE}${path}`, { method: 'DELETE' });
    return res.json();
}

// --- Discover (dispositivos disponibles) ---

async function refreshDiscover() {
    try {
        const data = await apiGet('/discover');
        const grid = document.getElementById('discover-grid');

        if (data.devices.length === 0) {
            grid.innerHTML = '<p class="empty-state">Todos los dispositivos han sido comisionados.</p>';
            return;
        }

        grid.innerHTML = '';
        data.devices.forEach(device => {
            grid.appendChild(createDiscoverCard(device));
        });
        renderQRCodes();
    } catch (err) {
        console.error('Error refreshing discover:', err);
    }
}

function createDiscoverCard(device) {
    const card = document.createElement('div');
    card.className = `device-card card available ${device.type}`;
    card.id = `discover-${device.node_id}`;

    card.innerHTML = `
        <div class="device-header">
            <span class="device-name">${device.name}</span>
            <span class="device-type">${getTypeLabel(device.type)} (nodo ${device.node_id})</span>
        </div>
        <div class="device-state qr-section">
            <div id="qr-${device.node_id}" class="qr-container"></div>
            <code>${device.setup_code}</code>
        </div>
        <div class="device-controls">
            <button class="primary" onclick="commissionDevice(${device.node_id}, '${device.setup_code}')">Comisionar</button>
        </div>
    `;

    return card;
}

function renderQRCodes() {
    document.querySelectorAll('.qr-container').forEach(container => {
        if (container.childElementCount > 0) return; // ya renderizado
        const code = container.closest('.qr-section')?.querySelector('code')?.textContent;
        if (code && typeof QRCode !== 'undefined') {
            new QRCode(container, { text: code, width: 80, height: 80, correctLevel: QRCode.CorrectLevel.L });
        }
    });
}

async function commissionDevice(nodeId, setupCode) {
    const result = await apiPost(`/devices/${nodeId}/commission`, { setup_code: setupCode });
    if (result.status === 'commissioned') {
        addEventLog(`Comisionado: ${result.name} (nodo ${nodeId})`, result.type);
        refreshDiscover();
        refreshDevices();
    } else {
        addEventLog(`Error comisionando nodo ${nodeId}`, JSON.stringify(result), true);
    }
}

async function decommissionDevice(nodeId) {
    const result = await apiDelete(`/devices/${nodeId}/commission`);
    if (result.status === 'decommissioned') {
        addEventLog(`Descomisionado: nodo ${nodeId}`, '');
        refreshDiscover();
        refreshDevices();
    }
}

// --- Dispositivos comisionados ---

async function refreshDevices() {
    try {
        const data = await apiGet('/devices');
        const grid = document.getElementById('devices-grid');

        if (data.devices.length === 0) {
            grid.innerHTML = '<p class="empty-state">Ninguno comisionado aun. Comisiona dispositivos desde la seccion de arriba.</p>';
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
            <button class="decommission" onclick="decommissionDevice(${device.node_id})" title="Descomisionar">Descomisionar</button>
        </div>
    `;

    return card;
}

function getTypeLabel(type) {
    const labels = {
        lighting: 'Bombilla',
        switch: 'Interruptor',
        lock: 'Cerradura',
        contact_sensor: 'Sensor contacto',
        window: 'Persiana',
        media_player: 'Smart TV',
        smoke: 'Sensor humo',
        water_leak: 'Sensor agua',
        temperature: 'Sensor temp.',
        thermostat: 'Termostato',
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
                    <input type="range" min="0" max="254" value="${device.state.brightness || 0}"
                           onchange="sendCmd(${nid}, 'brightness', [this.value])">
                </label>
                <label style="width:48%">Hue
                    <input type="range" min="0" max="254" value="${device.state.hue || 0}"
                           onchange="sendCmd(${nid}, 'color', [this.value, document.getElementById('sat-${nid}').value])">
                </label>
                <label style="width:48%">Saturacion
                    <input type="range" min="0" max="254" value="${device.state.saturation || 0}" id="sat-${nid}"
                           onchange="sendCmd(${nid}, 'color', [document.querySelector('[onchange*=\\'sat-${nid}\\']')?.value || '0', this.value])">
                </label>
            `;
        case 'switch':
            return `
                <button class="primary" onclick="sendCmd(${nid}, 'on')">Encender</button>
                <button onclick="sendCmd(${nid}, 'off')">Apagar</button>
                <button onclick="sendCmd(${nid}, 'toggle')">Toggle</button>
            `;
        case 'lock':
            return `
                <button class="danger" onclick="sendCmd(${nid}, 'unlock')">Abrir</button>
                <button class="primary" onclick="sendCmd(${nid}, 'lock')">Cerrar</button>
            `;
        case 'contact_sensor':
            return `
                <button class="danger" onclick="triggerEvent(${nid}, 'contact-open')">Abrir contacto</button>
                <button class="primary" onclick="triggerEvent(${nid}, 'contact-close')">Cerrar contacto</button>
            `;
        case 'thermostat':
            return `
                <button onclick="sendCmd(${nid}, 'read')">Leer</button>
                <label>Setpoint (x100)
                    <input type="number" id="therm-${nid}" value="${device.state.occupied_heating_setpoint || 2200}" style="width:80px">
                </label>
                <button class="primary" onclick="sendCmd(${nid}, 'set', [document.getElementById('therm-${nid}').value])">Ajustar</button>
            `;
        case 'window':
            return `
                <label style="width:100%">Apertura (%)
                    <input type="range" min="0" max="100" value="${device.state.current_position_lift_percentage || 0}"
                           onchange="sendCmd(${nid}, 'set', [this.value])">
                </label>
                <button class="primary" onclick="sendCmd(${nid}, 'open')">Abrir</button>
                <button onclick="sendCmd(${nid}, 'close')">Cerrar</button>
            `;
        case 'media_player':
            return `
                <button class="primary" onclick="sendCmd(${nid}, 'on')">Encender</button>
                <button onclick="sendCmd(${nid}, 'off')">Apagar</button>
                <label style="width:100%">URL
                    <input type="text" id="media-url-${nid}" placeholder="https://youtube.com/..." style="width:100%">
                </label>
                <button class="primary" onclick="sendCmd(${nid}, 'play', [document.getElementById('media-url-${nid}').value])">Play</button>
                <button onclick="sendCmd(${nid}, 'pause')">Pausa</button>
                <button onclick="sendCmd(${nid}, 'stop')">Stop</button>
            `;
        case 'smoke':
            return `
                <button class="danger" onclick="triggerEvent(${nid}, 'smoke-alarm')">Disparar alarma</button>
                <button onclick="triggerEvent(${nid}, 'smoke-clear')">Limpiar alarma</button>
            `;
        case 'water_leak':
            return `
                <button class="danger" onclick="triggerEvent(${nid}, 'water-leak')">Detectar fuga</button>
                <button onclick="triggerEvent(${nid}, 'water-clear')">Limpiar fuga</button>
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
    addEventLog(`Comando: ${command} -> nodo ${nodeId}`, JSON.stringify(result.result));
    refreshDevices();
}

async function triggerEvent(nodeId, eventType) {
    const result = await apiPost(`/devices/${nodeId}/trigger`, {
        event_type: eventType,
    });
    addEventLog(`Evento: ${eventType} -> nodo ${nodeId}`, JSON.stringify(result.result), true);
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

setInterval(() => {
    refreshDevices();
    refreshDiscover();
}, 5000);

refreshDiscover();
refreshDevices();
