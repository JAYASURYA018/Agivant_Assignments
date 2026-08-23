

let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    currentUser = checkSessionAndRedirect(true);
    if (!currentUser) return;

    initProfileDropdown(true);

    setupDateListeners();
    setupSubmitListener();
});

function setupDateListeners() {
    const startInput = document.getElementById('start-date');
    const endInput = document.getElementById('end-date');
    const daysBadge = document.getElementById('days-badge');
    const dateError = document.getElementById('date-error');

    async function calculateDays() {
        const startDateVal = startInput.value;
        const endDateVal = endInput.value;

        
        daysBadge.style.display = 'none';
        dateError.style.display = 'none';
        startInput.classList.remove('is-invalid');
        endInput.classList.remove('is-invalid');

        if (!startDateVal || !endDateVal) {
            return;
        }

        try {
            
            const days = await apiRequest(`/leaves/calculate-days?startDate=${startDateVal}&endDate=${endDateVal}`);

            daysBadge.textContent = `Leave Days: ${days}`;
            daysBadge.style.display = 'inline-block';
        } catch (error) {
            dateError.textContent = error.message || 'Invalid date range.';
            dateError.style.display = 'block';
            startInput.classList.add('is-invalid');
            endInput.classList.add('is-invalid');
        }
    }

    startInput.addEventListener('change', calculateDays);
    endInput.addEventListener('change', calculateDays);
}

function setupSubmitListener() {
    const btnSubmit = document.getElementById('btn-submit-leave');

    btnSubmit.addEventListener('click', async () => {
        if (!validateForm()) {
            return;
        }

        const leaveType = document.getElementById('leave-type').value;
        const startDate = document.getElementById('start-date').value;
        const endDate = document.getElementById('end-date').value;
        const reason = document.getElementById('reason').value;

        const payload = {
            employeeId: currentUser.id,
            leaveType: leaveType,
            startDate: startDate,
            endDate: endDate,
            reason: reason
        };

        try {
            btnSubmit.disabled = true;
            btnSubmit.textContent = 'Submitting...';

            await apiRequest('/leaves', {
                method: 'POST',
                body: payload
            });

            showToast('Leave request applied successfully!', 'success');

            
            setTimeout(() => {
                window.location.href = '../index.html';
            }, 450);

        } catch (error) {
            btnSubmit.disabled = false;
            btnSubmit.textContent = 'Submit';

            
            showToast(error.message || 'Failed to submit leave request.', 'error');
        }
    });
}

function validateForm() {
    let isValid = true;

    const leaveTypeSelect = document.getElementById('leave-type');
    const startInput = document.getElementById('start-date');
    const endInput = document.getElementById('end-date');

    const typeError = document.getElementById('leave-type-error');
    const dateError = document.getElementById('date-error');

    
    leaveTypeSelect.classList.remove('is-invalid');
    startInput.classList.remove('is-invalid');
    endInput.classList.remove('is-invalid');

    typeError.style.display = 'none';
    dateError.style.display = 'none';

    
    if (!leaveTypeSelect.value) {
        leaveTypeSelect.classList.add('is-invalid');
        typeError.style.display = 'block';
        isValid = false;
    }

    
    if (!startInput.value || !endInput.value) {
        dateError.textContent = 'Start date and End date are required.';
        dateError.style.display = 'block';
        if (!startInput.value) startInput.classList.add('is-invalid');
        if (!endInput.value) endInput.classList.add('is-invalid');
        isValid = false;
    }

    return isValid;
}
