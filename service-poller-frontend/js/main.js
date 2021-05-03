const BACKEND_BASE_ADDRESS = 'http://localhost:8888';
const DATA_SERVICE_ID_ATTR = 'data-service-id';

const servicesTable = document.querySelector('.services-table');
const servicesTableTbody = servicesTable.querySelector('tbody')
const addServiceModal = document.getElementById('addServiceModal');

document.addEventListener('DOMContentLoaded', () => {
    updateServices();
    setInterval(updateServices, 5 * 1000);

    const modalAddServiceButton = document.getElementById('modalAddServiceButton');
    modalAddServiceButton.addEventListener('click', handleAddServiceButtonClick)
    addServiceModal.addEventListener('shown.bs.modal', function () {
        document.getElementById('serviceName').focus();
    });

    servicesTable.addEventListener('click', e => {
        const target = e.target;
        const isDeleteButton = target.classList.contains('delete-service-button');
        if (isDeleteButton) {
            handleDeleteButtonClick(target);
        }
    });
});

const updateServices = () => {
    fetch(`${BACKEND_BASE_ADDRESS}/api/services`)
        .then(response => response.json())
        .then(updateServiceTable);
};

const updateServiceTable = services => {
    servicesTableTbody.textContent = '';
    services
        .forEach(createServiceRow);
};

const createServiceRow = service => {
    const row = servicesTableTbody.insertRow(-1);
    row.setAttribute(DATA_SERVICE_ID_ATTR, service.id);
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

    const cellActions = row.insertCell(cellIndex++);
    const btnDelete = document.createElement('button');
    btnDelete.classList.add('btn', 'btn-danger', 'delete-service-button');
    btnDelete.setAttribute(DATA_SERVICE_ID_ATTR, service.id);
    btnDelete.textContent = 'Delete';
    cellActions.appendChild(btnDelete);
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

const handleDeleteButtonClick = btn => {
    const serviceId = btn.getAttribute(DATA_SERVICE_ID_ATTR);
    fetch(`${BACKEND_BASE_ADDRESS}/api/services/${serviceId}`, {
        method: 'DELETE',
    })
        .then(response => {
            if (response.status === 200) {
                const serviceRow = document.querySelector(`tr[${DATA_SERVICE_ID_ATTR}="${serviceId}"]`);
                serviceRow.remove();
            }
        });


}
