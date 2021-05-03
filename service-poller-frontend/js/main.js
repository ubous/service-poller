const BACKEND_BASE_ADDRESS = 'http://localhost:8888';
const DATA_SERVICE_ID_ATTR = 'data-service-id';
const DATA_SERVICE_NAME_ATTR = 'data-service-name';
const DATA_SERVICE_URL_ATTR = 'data-service-url';


const servicesTable = document.querySelector('.services-table');
const servicesTableTbody = servicesTable.querySelector('tbody')

const addServiceModal = document.getElementById('addServiceModal');

const editServiceModal = document.getElementById('editServiceModal');
const editServiceName = document.getElementById('editServiceName');
const editServiceUrl = document.getElementById('editServiceUrl');
const editServiceId = document.getElementById('editServiceId');

document.addEventListener('DOMContentLoaded', () => {
    updateServices();
    setInterval(updateServices, 5 * 1000);

    const modalAddServiceButton = document.getElementById('modalAddServiceButton');
    modalAddServiceButton.addEventListener('click', handleAddServiceButtonClick)
    addServiceModal.addEventListener('shown.bs.modal', function () {
        document.getElementById('serviceName').focus();
    });

    const modalEditServiceButton = document.getElementById('modalEditServiceButton');
    modalEditServiceButton.addEventListener('click', saveEditChanges);
    editServiceModal.addEventListener('shown.bs.modal', function () {
        document.getElementById('editServiceName').focus();
    });

    servicesTable.addEventListener('click', e => {
        const target = e.target;
        const isDeleteButton = target.classList.contains('delete-service-button');
        if (isDeleteButton) {
            handleDeleteButtonClick(target);
            e.stopPropagation();
            return;
        }
        const isEditButton = target.classList.contains('edit-service-button');
        if (isEditButton) {
            handleEditButtonClick(target);
            e.stopPropagation();
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
    const btnEdit = document.createElement('button');
    btnEdit.classList.add('btn', 'btn-primary', 'edit-service-button');
    btnEdit.setAttribute(DATA_SERVICE_ID_ATTR, service.id);
    btnEdit.setAttribute(DATA_SERVICE_NAME_ATTR, service.name);
    btnEdit.setAttribute(DATA_SERVICE_URL_ATTR, service.url);
    btnEdit.textContent = 'Edit';
    cellActions.appendChild(btnEdit);

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

const handleEditButtonClick = btn => {
    editServiceId.value = btn.getAttribute(DATA_SERVICE_ID_ATTR);
    editServiceName.value = btn.getAttribute(DATA_SERVICE_NAME_ATTR);
    editServiceUrl.value = btn.getAttribute(DATA_SERVICE_URL_ATTR);
    const editModal = new bootstrap.Modal(editServiceModal);
    editModal.show();
}

const saveEditChanges = () => {
    const serviceId = editServiceId.value;
    const requestBody = {
        name: editServiceName.value,
        url: editServiceUrl.value,
    };
    fetch(`${BACKEND_BASE_ADDRESS}/api/services/${serviceId}`, {
        method: 'PUT',
        body: JSON.stringify(requestBody),
    })
        .then(response => response.json())
        .then(service => {
            const serviceCols = servicesTableTbody.querySelectorAll(`tr[${DATA_SERVICE_ID_ATTR}="${serviceId}"] td`);
            serviceCols[0].textContent = service.name;
            serviceCols[1].textContent = service.url;

            const editModal = bootstrap.Modal.getInstance(editServiceModal);
            editModal.hide();
        })
};
