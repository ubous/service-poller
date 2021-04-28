const BACKEND_BASE_ADDRESS = 'http://localhost:8888'

const servicesContainer = document.querySelector('.services-container');

const createServiceItem = service => {
    const li = document.createElement('li');
    li.classList.add('list-group-item');
    li.setAttribute('data-id', service.id);
    li.textContent = `${service.name} - ${service.url} - ${service.status} - ${service.statusUpdateTime}`;

    return li;
}

const updateServices = () => {
    fetch(`${BACKEND_BASE_ADDRESS}/api/services`)
        .then(response => response.json())
        .then(services => {
            const fragment = document.createDocumentFragment();
            services
                .map(createServiceItem)
                .forEach(item => fragment.appendChild(item));
            servicesContainer.textContent = '';
            servicesContainer.appendChild(fragment);
        });
};

document.addEventListener('DOMContentLoaded', () => {
    updateServices();
    setInterval(updateServices, 5 * 1000);
})
