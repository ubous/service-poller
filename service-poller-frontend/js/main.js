const BACKEND_BASE_ADDRESS = 'http://localhost:8888'

const servicesTable = document.querySelector('.services-table tbody');
const addServiceModal = document.getElementById('addServiceModal');

document.addEventListener('DOMContentLoaded', () => {
    updateServices();
    setInterval(updateServices, 5 * 1000);

    const modalAddServiceButton = document.getElementById('modalAddServiceButton');
    modalAddServiceButton.addEventListener('click', handleAddServiceButtonClick)
    addServiceModal.addEventListener('shown.bs.modal', function () {
        document.getElementById('serviceName').focus();
    })
});

const updateServices = () => {
    fetch(`${BACKEND_BASE_ADDRESS}/api/services`)
        .then(response => response.json())
        .then(updateServiceTable);
};

const updateServiceTable = services => {
    servicesTable.textContent = '';
    services
        .forEach(createServiceRow);
};

const createServiceRow = service => {
    const row = servicesTable.insertRow(-1);
    let cellIndex = 0;

    const cellName = row.insertCell(cellIndex++);
    cellName.textContent = service.name;

    const cellUrl = row.insertCell(cellIndex++);
    cellUrl.textContent = service.url;

    const cellStatus = row.insertCell(cellIndex++);
    const badge = document.createElement('span');
    const badgeClass = getBadgeClass(service.status);
    badge.classList.add('badge', badgeClass);
    badge.textContent = service.status;

    cellStatus.appendChild(badge);

    const cellStatusUpdateTime = row.insertCell(cellIndex++);
    cellStatusUpdateTime.textContent = service.statusUpdateTime;
};

const getBadgeClass = serviceStatus => {
    switch (serviceStatus) {
        case 'OK':
            return 'bg-success';
        case 'FAIL':
            return 'bg-danger';
    }
    return 'bg-secondary';
};

const handleAddServiceButtonClick = () => {
    const serviceNameInput = document.getElementById('serviceName');
    const serviceUrlInput = document.getElementById('serviceUrl');

    const requestBody = {
        name: serviceNameInput.value,
        url: serviceUrlInput.value,
    };

    console.log(JSON.stringify(requestBody));
    fetch(`${BACKEND_BASE_ADDRESS}/api/services`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody),
    })
        .then(response => response.json())
        .then(service => {
            createServiceRow(service);
            serviceNameInput.value = '';
            serviceUrlInput.value = '';
            const modal = bootstrap.Modal.getInstance(addServiceModal);
            modal.hide();
        });
};
