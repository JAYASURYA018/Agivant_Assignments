

document.addEventListener('DOMContentLoaded', () => {
    setupRegisterListener();
});

function setupRegisterListener() {
    const btnSubmit = document.getElementById('btn-register-submit');
    const form = document.getElementById('register-form');

    btnSubmit.addEventListener('click', async () => {
        if (!validateForm()) {
            return;
        }

        const firstName = document.getElementById('reg-first-name').value.trim();
        const lastName = document.getElementById('reg-last-name').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const department = document.getElementById('reg-department').value;
        const role = document.getElementById('reg-role').value;
        const password = document.getElementById('reg-password').value;

        const payload = {
            firstName: firstName,
            lastName: lastName,
            email: email,
            password: password,
            department: department,
            role: role
        };

        try {
            btnSubmit.disabled = true;
            btnSubmit.textContent = 'Registering...';

            await apiRequest('/employees', {
                method: 'POST',
                body: payload
            });

            showToast('Account registered successfully! Redirecting to sign in...', 'success');

            
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 400);

        } catch (error) {
            btnSubmit.disabled = false;
            btnSubmit.textContent = 'Register';
            showToast(error.message || 'Registration failed. Please try again.', 'error');
        }
    });
}

function validateForm() {
    let isValid = true;

    const firstNameInput = document.getElementById('reg-first-name');
    const lastNameInput = document.getElementById('reg-last-name');
    const emailInput = document.getElementById('reg-email');
    const deptSelect = document.getElementById('reg-department');
    const roleSelect = document.getElementById('reg-role');
    const passwordInput = document.getElementById('reg-password');

    const firstNameError = document.getElementById('reg-first-name-error');
    const lastNameError = document.getElementById('reg-last-name-error');
    const emailError = document.getElementById('reg-email-error');
    const deptError = document.getElementById('reg-department-error');
    const roleError = document.getElementById('reg-role-error');
    const passwordError = document.getElementById('reg-password-error');

    
    firstNameInput.classList.remove('is-invalid');
    lastNameInput.classList.remove('is-invalid');
    emailInput.classList.remove('is-invalid');
    deptSelect.classList.remove('is-invalid');
    roleSelect.classList.remove('is-invalid');
    passwordInput.classList.remove('is-invalid');

    firstNameError.style.display = 'none';
    lastNameError.style.display = 'none';
    emailError.style.display = 'none';
    deptError.style.display = 'none';
    roleError.style.display = 'none';
    passwordError.style.display = 'none';

    
    if (!firstNameInput.value.trim()) {
        firstNameInput.classList.add('is-invalid');
        firstNameError.style.display = 'block';
        isValid = false;
    }

    
    if (!lastNameInput.value.trim()) {
        lastNameInput.classList.add('is-invalid');
        lastNameError.style.display = 'block';
        isValid = false;
    }

    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailInput.value.trim() || !emailRegex.test(emailInput.value.trim())) {
        emailInput.classList.add('is-invalid');
        emailError.style.display = 'block';
        isValid = false;
    }

    
    if (!deptSelect.value) {
        deptSelect.classList.add('is-invalid');
        deptError.style.display = 'block';
        isValid = false;
    }

    
    if (!roleSelect.value) {
        roleSelect.classList.add('is-invalid');
        roleError.style.display = 'block';
        isValid = false;
    }

    
    if (!passwordInput.value || passwordInput.value.length < 6) {
        passwordInput.classList.add('is-invalid');
        passwordError.style.display = 'block';
        isValid = false;
    }

    return isValid;
}
