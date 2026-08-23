

function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            display: flex;
            flex-direction: column;
            gap: 10px;
            pointer-events: none;
        `;
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.style.cssText = `
        padding: 12px 20px;
        border-radius: 8px;
        color: #ffffff;
        font-size: 14px;
        font-weight: 500;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        display: flex;
        align-items: center;
        gap: 10px;
        opacity: 0;
        transform: translateY(-20px);
        transition: all 0.3s cubic-bezier(0.68, -0.55, 0.27, 1.55);
        pointer-events: auto;
    `;

    let icon = '✓';
    let bgColor = '#10b981'; 
    if (type === 'error') {
        icon = '✕';
        bgColor = '#ef4444'; 
    } else if (type === 'warning') {
        icon = '⚠';
        bgColor = '#f59e0b'; 
    } else if (type === 'info') {
        icon = 'ℹ';
        bgColor = '#3b82f6'; 
    }

    toast.style.backgroundColor = bgColor;
    toast.innerHTML = `
        <span style="font-weight: bold; font-size: 16px;">${icon}</span>
        <span>${message}</span>
    `;

    container.appendChild(toast);

    
    requestAnimationFrame(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
    });

    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-20px)';
        toast.addEventListener('transitionend', () => {
            toast.remove();
        });
    }, 4000);
}

function checkSessionAndRedirect(isPagesFolder = false) {
    const userJson = localStorage.getItem('currentUser');
    if (!userJson) {
        const loginPath = isPagesFolder ? 'login.html' : 'pages/login.html';
        window.location.href = loginPath;
        return null;
    }
    return JSON.parse(userJson);
}

function logout(isPagesFolder = false) {
    localStorage.removeItem('currentUser');
    showToast('Signed out successfully.', 'info');
    setTimeout(() => {
        const loginPath = isPagesFolder ? 'login.html' : 'pages/login.html';
        window.location.href = loginPath;
    }, 1000);
}

function initProfileDropdown(isPagesFolder = false) {
    const avatar = document.getElementById('avatar-img');
    const dropdown = document.getElementById('profile-dropdown');
    const logoutBtn = document.getElementById('dropdown-logout-btn');

    if (!avatar || !dropdown) return;

    
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (currentUser) {
        const nameEl = document.getElementById('dropdown-user-name');
        const emailEl = document.getElementById('dropdown-user-email');
        if (nameEl) nameEl.textContent = `${currentUser.firstName} ${currentUser.lastName}`;
        if (emailEl) emailEl.textContent = currentUser.email;

        
        if (!document.getElementById('dropdown-profile-btn')) {
            const profileBtn = document.createElement('button');
            profileBtn.id = 'dropdown-profile-btn';
            profileBtn.className = 'dropdown-item-btn';
            profileBtn.textContent = 'My Profile';
            
            const profileUrl = isPagesFolder ? 'profile.html' : 'pages/profile.html';
            profileBtn.addEventListener('click', () => {
                window.location.href = profileUrl;
            });
            
            const divider = dropdown.querySelector('.dropdown-divider');
            if (divider) {
                dropdown.insertBefore(profileBtn, divider);
            }
        }

        
        if (currentUser.role === 'MANAGER' && !document.getElementById('dropdown-add-user-btn')) {
            const addUserBtn = document.createElement('button');
            addUserBtn.id = 'dropdown-add-user-btn';
            addUserBtn.className = 'dropdown-item-btn';
            addUserBtn.textContent = 'Add User';
            
            addUserBtn.addEventListener('click', () => {
                
                if (window.location.pathname.endsWith('employees.html')) {
                    if (typeof openEmployeeModal === 'function') {
                        openEmployeeModal();
                    }
                } else {
                    
                    const teamUrl = isPagesFolder ? 'employees.html?action=add' : 'pages/employees.html?action=add';
                    window.location.href = teamUrl;
                }
            });
            
            const divider = dropdown.querySelector('.dropdown-divider');
            if (divider) {
                dropdown.insertBefore(addUserBtn, divider);
            }
        }
    }

    avatar.addEventListener('click', (e) => {
        e.stopPropagation();
        dropdown.classList.toggle('show');
    });

    
    document.addEventListener('click', () => {
        dropdown.classList.remove('show');
    });

    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            logout(isPagesFolder);
        });
    }

    
    const searchBtn = document.getElementById('nav-search-btn');
    if (searchBtn) {
        searchBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const searchInput = document.getElementById('search-employee-input') || 
                                document.querySelector('input[type="search"]') ||
                                document.querySelector('input[placeholder*="Search"]') ||
                                document.querySelector('input[placeholder*="search"]');
            if (searchInput) {
                searchInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
                setTimeout(() => {
                    searchInput.focus();
                    searchInput.select();
                }, 200);
            } else {
                showToast('Use search options inside Team or Leave Requests tabs.', 'info');
            }
        });
    }
}
