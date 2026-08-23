

let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    
    currentUser = checkSessionAndRedirect(true);
    if (!currentUser) return;

    
    initProfileDropdown(true);

    
    loadHolidaysTable();
});

async function loadHolidaysTable() {
    const tableBody = document.getElementById('holiday-table-body');
    try {
        
        const holidays = await apiRequest('/holidays');

        tableBody.innerHTML = ''; 

        if (holidays.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="3" style="text-align: center; color: var(--text-muted); padding: 40px;">No public holidays configured.</td></tr>';
            return;
        }

        holidays.forEach(h => {
            const row = document.createElement('tr');
            
            
            const formattedDate = formatHolidayDate(h.holidayDate);

            row.innerHTML = `
                <td class="holiday-name-cell">${h.name}</td>
                <td class="holiday-date-cell">${formattedDate}</td>
                <td><span class="pill-badge" style="background-color: #f0fdf4; color: #166534; border-color: #dcfce7;">Holiday</span></td>
            `;
            tableBody.appendChild(row);
        });

    } catch (error) {
        tableBody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: #ef4444; padding: 40px;">Failed to load holidays: ${error.message}</td></tr>`;
        showToast('Error loading holiday list.', 'error');
    }
}

function formatHolidayDate(dateStr) {
    if (!dateStr) return '';
    try {
        const parts = dateStr.split('-');
        const year = parts[0];
        const monthIdx = parseInt(parts[1], 10) - 1;
        const day = parseInt(parts[2], 10);
        
        
        const date = new Date(year, monthIdx, day);
        
        const dayStr = String(day).padStart(2, '0');
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        const monthStr = months[monthIdx];
        
        const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
        const dayOfWeekStr = daysOfWeek[date.getDay()];
        
        return `${dayStr}-${monthStr}-${year}, ${dayOfWeekStr}`;
    } catch (e) {
        return dateStr;
    }
}
