

let currentUser = null;
let currentActiveDate = new Date('2026-08-17'); 

let loadedEmployees = [];
let currentSortColumn = 'id';
let currentSortAscending = true;

document.addEventListener('DOMContentLoaded', () => {
    currentUser = checkSessionAndRedirect(true);
    if (!currentUser) return;

    initProfileDropdown(true);
    setupWeeklyNavigation();
    setupTabSwitching();
    loadWeeklyLeaves();
    setupManagerCRUD();
});

function setupTabSwitching() {
    const tabOnLeave = document.getElementById('tab-on-leave');
    const tabAllEmployees = document.getElementById('tab-all-employees');
    
    const viewOnLeave = document.getElementById('on-leave-view');
    const viewAllEmployees = document.getElementById('all-employees-view');
    
    tabOnLeave.addEventListener('click', () => {
        tabOnLeave.classList.add('active');
        tabAllEmployees.classList.remove('active');
        viewOnLeave.style.display = 'block';
        viewAllEmployees.style.display = 'none';
        loadWeeklyLeaves();
    });
    
    tabAllEmployees.addEventListener('click', () => {
        tabAllEmployees.classList.add('active');
        tabOnLeave.classList.remove('active');
        viewOnLeave.style.display = 'none';
        viewAllEmployees.style.display = 'block';
        loadAllEmployees();
    });
}

async function loadAllEmployees() {
    try {
        const listBody = document.getElementById('employee-list-body');
        const isManager = currentUser && currentUser.role === 'MANAGER';
        const colspanVal = isManager ? 5 : 4;
        
        listBody.innerHTML = `<tr><td colspan="${colspanVal}" id="employee-colspan-td" style="text-align: center; color: var(--text-muted); padding: 40px;">Fetching employees...</td></tr>`;
        
        const dir = currentSortAscending ? 'asc' : 'desc';
        loadedEmployees = await apiRequest(`/employees?sortBy=${currentSortColumn}&direction=${dir}`);
        renderAllEmployeesTable();
    } catch (error) {
        const listBody = document.getElementById('employee-list-body');
        const isManager = currentUser && currentUser.role === 'MANAGER';
        const colspanVal = isManager ? 5 : 4;
        listBody.innerHTML = `<tr><td colspan="${colspanVal}" id="employee-colspan-td" style="text-align: center; color: #ef4444; padding: 40px;">Error: ${error.message}</td></tr>`;
        showToast('Failed to load employee list.', 'error');
    }
}

function renderAllEmployeesTable() {
    const listBody = document.getElementById('employee-list-body');
    listBody.innerHTML = '';

    const isManager = currentUser && currentUser.role === 'MANAGER';
    const colspanVal = isManager ? 5 : 4;

    
    if (isManager) {
        document.querySelectorAll('.actions-header').forEach(el => el.style.display = 'table-cell');
    } else {
        document.querySelectorAll('.actions-header').forEach(el => el.style.display = 'none');
    }

    if (loadedEmployees.length === 0) {
        listBody.innerHTML = `<tr><td colspan="${colspanVal}" id="employee-colspan-td" style="text-align: center; color: var(--text-muted); padding: 40px;">No employees registered.</td></tr>`;
        return;
    }

    loadedEmployees.forEach(emp => {
        const row = document.createElement('tr');
        const employeeIdDisplay = emp.employeeId || `EMP${emp.id}`;
        
        let actionCells = '';
        if (isManager) {
            
            const empJson = JSON.stringify(emp).replace(/"/g, '&quot;');
            actionCells = `
                <td style="text-align: right; padding-right: 20px; white-space: nowrap;">
                    <button class="action-btn-sm btn-approve-sm" style="background-color: #f1f5f9; color: #334155; border: 1px solid #cbd5e1;" onclick="openEmployeeModal(${empJson})">Edit</button>
                    <button class="action-btn-sm btn-reject-sm" style="background-color: #fef2f2; color: #ef4444; border: 1px solid #fca5a5;" onclick="triggerDeleteEmployee(${emp.id})">Delete</button>
                </td>
            `;
        }

        row.innerHTML = `
            <td style="font-weight: 600; color: #475569;">${employeeIdDisplay}</td>
            <td class="holiday-name-cell">${emp.firstName} ${emp.lastName}</td>
            <td class="holiday-date-cell">${emp.email}</td>
            <td><span class="pill-badge" style="background-color: #f1f5f9; color: #475569; border: 1px solid #cbd5e1;">${emp.department || '-'}</span></td>
            ${actionCells}
        `;
        listBody.appendChild(row);
    });

    updateSortIcons();
}

/**
 * Retrieve appropriate field for sorting comparison
 */
function getSortValue(emp, column) {
    switch (column) {
        case 'id':
            return emp.employeeId || String(emp.id);
        case 'name':
            return `${emp.firstName} ${emp.lastName}`.toLowerCase();
        case 'email':
            return emp.email.toLowerCase();
        case 'department':
            return (emp.department || '').toLowerCase();
        default:
            return '';
    }
}

/**
 * Handle user click on sortable headers
 */
function sortEmployees(column) {
    if (currentSortColumn === column) {
        currentSortAscending = !currentSortAscending;
    } else {
        currentSortColumn = column;
        currentSortAscending = true;
    }
    loadAllEmployees();
}

/**
 * Update UI arrow indicator icons next to header labels
 */
function updateSortIcons() {
    const columns = ['id', 'name', 'email', 'department'];
    columns.forEach(col => {
        const iconSpan = document.getElementById(`sort-icon-${col}`);
        if (!iconSpan) return;
        if (col === currentSortColumn) {
            iconSpan.textContent = currentSortAscending ? '▲' : '▼';
            iconSpan.style.color = '#0f172a';
        } else {
            iconSpan.textContent = '⇅';
            iconSpan.style.color = '#94a3b8';
        }
    });
}

/**
 * Configure weekly back and forward buttons
 */
function setupWeeklyNavigation() {
    const btnPrev = document.getElementById('btn-prev-week');
    const btnNext = document.getElementById('btn-next-week');

    btnPrev.addEventListener('click', () => {
        currentActiveDate.setDate(currentActiveDate.getDate() - 7);
        loadWeeklyLeaves();
    });

    btnNext.addEventListener('click', () => {
        currentActiveDate.setDate(currentActiveDate.getDate() + 7);
        loadWeeklyLeaves();
    });
}

/**
 * Fetch daily leaves from backend for active week and render them
 */
async function loadWeeklyLeaves() {
    const container = document.getElementById('leaves-weekly-container');
    const dateLabel = document.getElementById('label-date-range');

    // Calculate active week range (Sunday to Saturday)
    const sunday = getStartOfWeek(currentActiveDate);
    const saturday = getEndOfWeek(currentActiveDate);

    // Format headers range
    dateLabel.textContent = `${formatDateLabel(sunday)} - ${formatDateLabel(saturday)}`;

    // Query parameters formatted as ISO dates (YYYY-MM-DD)
    const startStr = formatDateISO(sunday);
    const endStr = formatDateISO(saturday);

    try {
        container.innerHTML = '<div style="text-align: center; color: var(--text-muted); padding: 40px;">Fetching daily schedules...</div>';
        
        // Fetch daily records from backend
        const dailyRecords = await apiRequest(`/leaves/on-leave?startDate=${startStr}&endDate=${endStr}`);
        
        container.innerHTML = ''; // Clear loading screen

        // We display weekdays Monday through Friday (typically index 1 to 5 of the week)
        const weekdays = getWeekdaysList(currentActiveDate);

        weekdays.forEach(dayDate => {
            const dateISO = formatDateISO(dayDate);
            // Match response day
            const dayRecord = dailyRecords.find(r => r.date === dateISO) || { leaveCount: 0, employees: [] };
            
            // Build the card element
            const card = document.createElement('div');
            card.className = 'leave-day-card';
            card.id = `card-${dateISO}`;

            // Check if day is today
            const isToday = formatDateISO(new Date()) === dateISO;
            const todayBadge = isToday ? '<span class="card-badge card-badge-today">Today</span>' : '';
            const bullet = isToday ? '<span class="card-bullet">•</span>' : '';

            card.innerHTML = `
                <div class="card-header-row" onclick="toggleCardExpansion('${dateISO}')">
                    <div class="card-title-info">
                        <span class="card-date-text">${formatDateLabel(dayDate)}</span>
                        ${todayBadge}
                        ${bullet}
                        <span class="card-bullet">•</span>
                        <span class="card-count-text">Leave count ${dayRecord.leaveCount}</span>
                    </div>
                    <div class="card-expand-icon">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    </div>
                </div>
                <div class="card-expansion-content" id="content-${dateISO}">
                    <!-- Render active leaves list -->
                    ${renderEmployeeLeavesList(dayRecord.employees)}
                </div>
            `;
            container.appendChild(card);
        });

    } catch (error) {
        container.innerHTML = `<div style="text-align: center; color: #ef4444; padding: 40px;">Failed to fetch weekly leave schedule: ${error.message}</div>`;
        showToast('Error loading schedule.', 'error');
    }
}

/**
 * Toggle expanding/collapsing details
 */
window.toggleCardExpansion = function(dateISO) {
    const card = document.getElementById(`card-${dateISO}`);
    if (card) {
        card.classList.toggle('open');
    }
};

/**
 * Renders the HTML representation of employees active on leave
 */
function renderEmployeeLeavesList(employees) {
    if (!employees || employees.length === 0) {
        return '<div class="no-leaves-placeholder">No employees on leave on this day.</div>';
    }

    return employees.map(emp => {
        // Style type badges
        const typeClass = `badge-${emp.leaveType.toLowerCase()}`;
        return `
            <div class="employee-leave-item">
                <div class="emp-meta-details">
                    <span class="emp-name">${emp.name}</span>
                    <span class="emp-dept">${emp.department}</span>
                </div>
                <div class="emp-leave-details">
                    <span class="leave-reason-text" title="${emp.reason || ''}">${emp.reason || 'No reason specified'}</span>
                    <span class="leave-type-badge ${typeClass}">${emp.leaveType}</span>
                </div>
            </div>
        `;
    }).join('');
}

/**
 * Return Sunday of the week containing date
 */
function getStartOfWeek(date) {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day; // day index (0 = Sunday, 1 = Monday...)
    return new Date(d.setDate(diff));
}

/**
 * Return Saturday of the week containing date
 */
function getEndOfWeek(date) {
    const d = getStartOfWeek(date);
    d.setDate(d.getDate() + 6);
    return d;
}

/**
 * Returns Monday through Friday dates list for the week containing reference date
 */
function getWeekdaysList(referenceDate) {
    const sunday = getStartOfWeek(referenceDate);
    const weekdays = [];
    for (let i = 1; i <= 5; i++) {
        const next = new Date(sunday);
        next.setDate(sunday.getDate() + i);
        weekdays.push(next);
    }
    return weekdays;
}

/**
 * Helper to format date label: e.g. 16-Aug-2026
 */
function formatDateLabel(date) {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const day = String(date.getDate()).padStart(2, '0');
    const month = months[date.getMonth()];
    const year = date.getFullYear();
    return `${day}-${month}-${year}`;
}

/**
 * Helper to format ISO date string: YYYY-MM-DD
 */
function formatDateISO(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/**
 * Setup Manager CRUD event listeners and modal toggle hooks
 */
function setupManagerCRUD() {
    const isManager = currentUser && currentUser.role === 'MANAGER';
    if (!isManager) return;

    const btnAdd = document.getElementById('btn-add-employee');
    const btnClose = document.getElementById('employee-modal-close');
    const btnCancel = document.getElementById('btn-cancel-employee');
    const btnSubmit = document.getElementById('btn-submit-employee');

    // Show add button
    if (btnAdd) {
        btnAdd.style.display = 'block';
        btnAdd.addEventListener('click', () => {
            openEmployeeModal();
        });
    }

    // Modal Close Triggers
    if (btnClose) btnClose.addEventListener('click', closeEmployeeModal);
    if (btnCancel) btnCancel.addEventListener('click', closeEmployeeModal);

    // Form Submit
    if (btnSubmit) {
        btnSubmit.addEventListener('click', submitEmployeeForm);
    }

    // Auto-trigger add modal if redirecting with ?action=add parameter
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('action') === 'add') {
        const tabAllEmployees = document.getElementById('tab-all-employees');
        if (tabAllEmployees) {
            tabAllEmployees.click();
        }
        openEmployeeModal();
    }
}

/**
 * Open Modal in either CREATE or EDIT mode
 */
function openEmployeeModal(emp = null) {
    const modal = document.getElementById('employee-modal');
    const title = document.getElementById('employee-modal-title');
    const hiddenIdInput = document.getElementById('employee-id-hidden');
    const pwdLabel = document.getElementById('emp-password-label');
    const pwdInput = document.getElementById('emp-password');

    // Reset Form
    document.getElementById('employee-form').reset();
    hiddenIdInput.value = '';

    if (emp) {
        // EDIT MODE
        title.textContent = 'Edit Employee';
        hiddenIdInput.value = emp.id;
        
        document.getElementById('emp-first-name').value = emp.firstName;
        document.getElementById('emp-last-name').value = emp.lastName;
        document.getElementById('emp-email').value = emp.email;
        document.getElementById('emp-department').value = emp.department || '';
        document.getElementById('emp-role').value = emp.role || 'EMPLOYEE';

        // Password is optional during edit
        pwdLabel.innerHTML = 'Password (leave blank to keep current)';
        pwdInput.required = false;
        pwdInput.placeholder = '••••••••';
    } else {
        // CREATE MODE
        title.textContent = 'Add Employee';
        pwdLabel.innerHTML = 'Password <span class="required-star">*</span>';
        pwdInput.required = true;
        pwdInput.placeholder = '••••••••';
    }

    modal.style.display = 'flex';
}

/**
 * Close Modal
 */
function closeEmployeeModal() {
    document.getElementById('employee-modal').style.display = 'none';
}

/**
 * Handle form submission (POST for create, PUT for update)
 */
async function submitEmployeeForm() {
    const hiddenIdInput = document.getElementById('employee-id-hidden');
    const id = hiddenIdInput.value;
    const isEdit = !!id;

    const firstName = document.getElementById('emp-first-name').value.trim();
    const lastName = document.getElementById('emp-last-name').value.trim();
    const email = document.getElementById('emp-email').value.trim();
    const password = document.getElementById('emp-password').value;
    const department = document.getElementById('emp-department').value.trim();
    const role = document.getElementById('emp-role').value;

    // Simple validation
    if (!firstName || !lastName || !email || (!isEdit && !password)) {
        showToast('Please fill in all required fields.', 'warning');
        return;
    }

    const payload = {
        firstName,
        lastName,
        email,
        password,
        department,
        role
    };

    try {
        const btnSubmit = document.getElementById('btn-submit-employee');
        btnSubmit.disabled = true;
        btnSubmit.textContent = 'Saving...';

        if (isEdit) {
            await apiRequest(`/employees/${id}`, {
                method: 'PUT',
                body: payload
            });
            showToast('Employee updated successfully!', 'success');
        } else {
            await apiRequest('/employees/createEmp', {
                method: 'POST',
                body: payload
            });
            showToast('Employee created successfully!', 'success');
        }

        closeEmployeeModal();
        loadAllEmployees(); // Refresh list
    } catch (e) {
        showToast(e.message || 'Error saving employee.', 'error');
    } finally {
        const btnSubmit = document.getElementById('btn-submit-employee');
        btnSubmit.disabled = false;
        btnSubmit.textContent = 'Save';
    }
}

/**
 * Trigger delete endpoint with verification check
 */
async function triggerDeleteEmployee(id) {
    if (currentUser && currentUser.id === id) {
        showToast('You cannot delete your own account!', 'warning');
        return;
    }

    if (confirm('Are you sure you want to delete this employee? This will automatically cancel all of their leave requests.')) {
        try {
            await apiRequest(`/employees/${id}`, { method: 'DELETE' });
            showToast('Employee deleted successfully!', 'success');
            loadAllEmployees(); // Refresh list
        } catch (e) {
            showToast(e.message || 'Error deleting employee.', 'error');
        }
    }
}
