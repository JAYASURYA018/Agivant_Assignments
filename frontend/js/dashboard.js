

let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    
    currentUser = checkSessionAndRedirect(false);
    if (!currentUser) return;

    
    initProfileDropdown(false);

    
    const insightSelect = document.getElementById('insight-select');
    if (insightSelect) {
        if (currentUser.role !== 'MANAGER') {
            insightSelect.style.display = 'none';
        } else {
            insightSelect.style.display = 'block';
        }
    }

    loadDashboardStats();
    loadUpcomingHolidays();
    setupTabSwitching();
    setupFilters();
});

let allLeaveRequests = [];

async function loadDashboardStats() {
    try {
        const stats = await apiRequest(`/dashboard/stats?employeeId=${currentUser.id}`);
        
        document.getElementById('booked-count').textContent = stats.leaveBookedThisYear;
        document.getElementById('absent-count').textContent = stats.absentToday;
        
        renderStatsCards(stats);
    } catch (error) {
        showToast('Failed to load dashboard statistics.', 'error');
    }
}

function renderStatsCards(stats) {
    document.getElementById('cl-available').textContent = stats.casualLeaveAvailable.toFixed(0);
    document.getElementById('cl-booked').textContent = stats.casualLeaveBooked.toFixed(0);

    document.getElementById('sl-available').textContent = stats.sickLeaveAvailable.toFixed(0);
    document.getElementById('sl-booked').textContent = stats.sickLeaveBooked.toFixed(0);

    document.getElementById('el-available').textContent = stats.earnedLeaveAvailable.toFixed(0);
    document.getElementById('el-booked').textContent = stats.earnedLeaveBooked.toFixed(0);
}

async function loadUpcomingHolidays() {
    try {
        const holidays = await apiRequest('/holidays/upcoming');
        const listContainer = document.getElementById('holiday-list');
        listContainer.innerHTML = '';

        if (holidays.length === 0) {
            listContainer.innerHTML = '<div class="holiday-row" style="grid-template-columns: 1fr; text-align: center; color: var(--text-muted);">No upcoming holidays.</div>';
            return;
        }

        holidays.forEach(holiday => {
            const row = document.createElement('div');
            row.className = 'holiday-row';
            const formattedDate = formatDateString(holiday.holidayDate);

            row.innerHTML = `
                <div class="holiday-date">${formattedDate}</div>
                <div class="holiday-name-cell">
                    <span class="holiday-icon">📅</span>
                    <span>${holiday.name}</span>
                </div>
            `;
            listContainer.appendChild(row);
        });
    } catch (error) {
        showToast('Failed to load upcoming holidays.', 'error');
    }
}

function setupTabSwitching() {
    const tabSummary = document.getElementById('tab-leave-summary');
    const tabRequests = document.getElementById('tab-leave-requests');
    const viewSummary = document.getElementById('leave-summary-view');
    const viewRequests = document.getElementById('leave-requests-view');

    tabSummary.addEventListener('click', () => {
        tabSummary.classList.add('active');
        tabRequests.classList.remove('active');
        viewSummary.style.display = 'block';
        viewRequests.style.display = 'none';
        loadDashboardStats(); 
    });

    tabRequests.addEventListener('click', () => {
        tabRequests.classList.add('active');
        tabSummary.classList.remove('active');
        viewSummary.style.display = 'none';
        viewRequests.style.display = 'block';
        loadLeaveRequests(); 
    });
}

async function loadLeaveRequests() {
    const listBody = document.getElementById('leave-requests-list-body');
    try {
        allLeaveRequests = await apiRequest('/leaves');
        renderLeaveRequestsTable(allLeaveRequests);
    } catch (error) {
        showToast('Failed to load leave requests directory.', 'error');
        listBody.innerHTML = '<tr><td colspan="9" style="text-align: center; color: #ef4444; padding: 20px;">Error loading leave requests.</td></tr>';
    }
}

function renderLeaveRequestsTable(requests) {
    applyFilters();
}

function renderStandardTable(requests) {
    const listBody = document.getElementById('leave-requests-list-body');
    listBody.innerHTML = '';

    if (requests.length === 0) {
        listBody.innerHTML = '<tr><td colspan="9" style="text-align: center; color: var(--text-muted); padding: 20px;">No leave requests found.</td></tr>';
        return;
    }

    requests.forEach(req => {
        const row = document.createElement('tr');
        const startDateFmt = formatDateString(req.startDate);
        const endDateFmt = formatDateString(req.endDate);

        
        let actionButtonsHtml = '';
        const isManager = currentUser && currentUser.role === 'MANAGER';
        const isOwnRequest = currentUser && req.employeeId === currentUser.id;

        if (isManager) {
            if (req.status === 'PENDING') {
                actionButtonsHtml = `
                    <button class="action-btn-sm btn-approve-sm" onclick="triggerWorkflow(${req.id}, 'approve')">Approve</button>
                    <button class="action-btn-sm btn-reject-sm" onclick="triggerWorkflow(${req.id}, 'reject')">Reject</button>
                    <button class="action-btn-sm btn-cancel-sm" onclick="triggerWorkflow(${req.id}, 'cancel')">Cancel</button>
                `;
            } else if (req.status === 'APPROVED') {
                actionButtonsHtml = `
                    <button class="action-btn-sm btn-cancel-sm" onclick="triggerWorkflow(${req.id}, 'cancel')">Cancel</button>
                `;
            } else {
                actionButtonsHtml = `<span style="color: var(--text-muted); font-size: 12px; font-style: italic;">No actions</span>`;
            }
        } else {
            
            if (isOwnRequest && (req.status === 'PENDING' || req.status === 'APPROVED')) {
                actionButtonsHtml = `
                    <button class="action-btn-sm btn-cancel-sm" onclick="triggerWorkflow(${req.id}, 'cancel')">Cancel</button>
                `;
            } else {
                actionButtonsHtml = `<span style="color: var(--text-muted); font-size: 12px; font-style: italic;">No actions</span>`;
            }
        }

        row.innerHTML = `
            <td style="font-weight: 600;">#${req.id}</td>
            <td>${req.employeeName}</td>
            <td><span style="font-weight: 500;">${req.leaveType}</span></td>
            <td>${startDateFmt}</td>
            <td>${endDateFmt}</td>
            <td><strong style="color: #1e40af;">${req.numberOfDays}</strong></td>
            <td><span class="status-badge badge-${req.status.toLowerCase()}">${req.status}</span></td>
            <td style="max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${req.reason || ''}">${req.reason || '-'}</td>
            <td style="text-align: right;">${actionButtonsHtml}</td>
        `;
        listBody.appendChild(row);
    });
}

async function triggerWorkflow(leaveId, action) {
    try {
        await apiRequest(`/leaves/${leaveId}/${action}`, { method: 'PUT' });
        showToast(`Leave request ${action}d successfully!`, 'success');
        
        
        loadLeaveRequests();
        loadDashboardStats();
    } catch (error) {
        showToast(error.message || `Failed to ${action} leave request.`, 'error');
    }
}

function setupFilters() {
    const searchInput = document.getElementById('search-employee-input');
    const typeSelect = document.getElementById('filter-type-select');
    const statusSelect = document.getElementById('filter-status-select');
    const insightSelect = document.getElementById('insight-select');

    async function applyFilters() {
        const insightMode = insightSelect.value;
        const query = searchInput.value.trim();
        const type = typeSelect.value;
        const status = statusSelect.value;

        const tableHeader = document.querySelector('#leave-requests-table thead');
        const listBody = document.getElementById('leave-requests-list-body');

        if (insightMode === 'standard' || insightMode === 'greater-days') {
            
            searchInput.disabled = false;
            typeSelect.disabled = false;
            statusSelect.disabled = false;
            
            if (insightMode === 'greater-days') {
                searchInput.placeholder = "Enter N (default 5)...";
            } else {
                searchInput.placeholder = "Search Employee...";
            }

            tableHeader.innerHTML = `
                <tr>
                    <th>ID</th>
                    <th>Employee</th>
                    <th>Type</th>
                    <th>Start Date</th>
                    <th>End Date</th>
                    <th>Days</th>
                    <th>Status</th>
                    <th>Reason</th>
                    <th style="text-align: right; padding-right: 20px;">Actions</th>
                </tr>
            `;

            const filtered = allLeaveRequests.filter(req => {
                
                if (currentUser && currentUser.role !== 'MANAGER') {
                    if (req.employeeId !== currentUser.id) {
                        return false;
                    }
                }

                let matchesSearch = true;
                if (insightMode === 'greater-days') {
                    const limit = parseInt(query, 10);
                    const n = isNaN(limit) ? 5 : limit;
                    matchesSearch = req.numberOfDays > n;
                } else {
                    matchesSearch = req.employeeName.toLowerCase().includes(query.toLowerCase());
                }

                const matchesType = !type || req.leaveType === type;
                const matchesStatus = !status || req.status === status;
                return matchesSearch && matchesType && matchesStatus;
            });

            renderStandardTable(filtered);

        } else if (insightMode === 'total-leaves') {
            
            searchInput.disabled = false;
            searchInput.placeholder = "Min Leaves (e.g. 5)...";
            typeSelect.disabled = true;
            statusSelect.disabled = true;

            tableHeader.innerHTML = `
                <tr>
                    <th>Employee ID</th>
                    <th>Employee Name</th>
                    <th>Total Approved Days</th>
                    <th style="text-align: right; padding-right: 20px;">Number of Requests</th>
                </tr>
            `;

            listBody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 20px;">Loading leaves report...</td></tr>';
            
            const limit = parseInt(query, 10);
            const minDays = isNaN(limit) ? 0 : limit;

            try {
                const summary = await apiRequest(`/dashboard/reports/total-leaves?minDays=${minDays}`);
                listBody.innerHTML = '';

                if (summary.length === 0) {
                    listBody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 20px;">No employees match the criteria.</td></tr>';
                    return;
                }

                summary.forEach(item => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td style="font-weight: 600; color: #475569;">${item.employeeId}</td>
                        <td>${item.name}</td>
                        <td><strong style="color: #059669;">${item.totalDays} Days</strong></td>
                        <td style="text-align: right; padding-right: 20px;">${item.requestsCount} approved</td>
                    `;
                    listBody.appendChild(row);
                });
            } catch (e) {
                listBody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #ef4444; padding: 20px;">Error loading report.</td></tr>';
            }

        } else if (insightMode === 'no-leaves') {
            
            searchInput.disabled = true;
            searchInput.placeholder = "Insight Active";
            typeSelect.disabled = true;
            statusSelect.disabled = true;

            tableHeader.innerHTML = `
                <tr>
                    <th>Employee ID</th>
                    <th>Employee Name</th>
                    <th>Email ID</th>
                    <th style="text-align: right; padding-right: 20px;">Department</th>
                </tr>
            `;

            listBody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 20px;">Analyzing leave history...</td></tr>';
            
            try {
                const noLeavesEmployees = await apiRequest('/dashboard/reports/no-leaves');
                listBody.innerHTML = '';

                if (noLeavesEmployees.length === 0) {
                    listBody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 20px;">All registered employees have submitted at least one leave request.</td></tr>';
                    return;
                }

                noLeavesEmployees.forEach(emp => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td style="font-weight: 600; color: #475569;">${emp.employeeId}</td>
                        <td>${emp.name}</td>
                        <td>${emp.email}</td>
                        <td style="text-align: right; padding-right: 20px;"><span class="pill-badge" style="background-color: #f1f5f9; color: #475569; padding: 4px 8px; border-radius: 4px; border: 1px solid #e2e8f0; font-size: 12px;">${emp.department || '-'}</span></td>
                    `;
                    listBody.appendChild(row);
                });
            } catch (e) {
                listBody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #ef4444; padding: 20px;">Error loading report.</td></tr>';
            }

        } else if (insightMode === 'frequent-type') {
            
            searchInput.disabled = true;
            searchInput.placeholder = "Insight Active";
            typeSelect.disabled = true;
            statusSelect.disabled = true;

            tableHeader.innerHTML = `
                <tr>
                    <th>Leave Type</th>
                    <th>Total Requests</th>
                    <th>Total Days Requested</th>
                    <th style="text-align: right; padding-right: 20px;">Popularity Status</th>
                </tr>
            `;

            listBody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 20px;">Analyzing leave categories...</td></tr>';

            try {
                const stats = await apiRequest('/dashboard/reports/frequent-type');
                listBody.innerHTML = '';

                stats.forEach(data => {
                    const badgeHtml = data.isWinner 
                        ? `<span style="background-color: #d1fae5; color: #065f46; font-weight: bold; padding: 4px 10px; border-radius: 12px; font-size: 12px;">Most Popular 🔥</span>`
                        : `<span style="color: #64748b; font-style: italic; font-size: 12px;">Standard</span>`;

                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td style="font-weight: bold; color: #1e3a8a;">${data.leaveType} (${data.label})</td>
                        <td><strong>${data.count}</strong> requests</td>
                        <td><strong>${data.days}</strong> days</td>
                        <td style="text-align: right; padding-right: 20px;">${badgeHtml}</td>
                    `;
                    listBody.appendChild(row);
                });
            } catch (e) {
                listBody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #ef4444; padding: 20px;">Error loading report.</td></tr>';
            }
        }
    }

    searchInput.addEventListener('input', applyFilters);
    typeSelect.addEventListener('change', applyFilters);
    statusSelect.addEventListener('change', applyFilters);
    insightSelect.addEventListener('change', applyFilters);

    
    window.applyFilters = applyFilters;
}

function formatDateString(dateStr) {
    const parts = dateStr.split('-');
    if (parts.length !== 3) return dateStr;

    const year = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10) - 1;
    const day = parseInt(parts[2], 10);

    const date = new Date(year, month, day);

    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    const formattedDay = String(day).padStart(2, '0');
    const formattedMonth = months[month];
    const dayOfWeek = days[date.getDay()];

    return `${formattedDay}-${formattedMonth}-${year}, ${dayOfWeek}`;
}
