

let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    currentUser = checkSessionAndRedirect(true);
    if (!currentUser) return;

    
    initProfileDropdown(true);

    
    populateProfileData();

    
    setupSubmitListener();
});

function populateProfileData() {
    document.getElementById('profile-email').value = currentUser.email || '';
    document.getElementById('profile-first-name').value = currentUser.firstName || '';
    document.getElementById('profile-last-name').value = currentUser.lastName || '';
}

function setupSubmitListener() {
    const btnSubmit = document.getElementById('btn-submit-profile');
    
    btnSubmit.addEventListener('click', async () => {
        if (!validateForm()) {
            return;
        }

        const firstName = document.getElementById('profile-first-name').value.trim();
        const lastName = document.getElementById('profile-last-name').value.trim();
        const password = document.getElementById('profile-password').value;

        
        const payload = {
            firstName: firstName,
            lastName: lastName,
            email: currentUser.email,
            department: currentUser.department,
            role: currentUser.role,
            password: password ? password : '' 
        };

        try {
            btnSubmit.disabled = true;
            btnSubmit.textContent = 'Saving...';

            const updatedUser = await apiRequest(`/employees/${currentUser.id}`, {
                method: 'PUT',
                body: payload
            });

            
            localStorage.setItem('currentUser', JSON.stringify(updatedUser));
            
            showToast('Profile updated successfully!', 'success');
            
            
            setTimeout(() => {
                window.location.href = '../index.html';
            }, 400);

        } catch (error) {
            btnSubmit.disabled = false;
            btnSubmit.textContent = 'Save Changes';
            showToast(error.message || 'Failed to update profile.', 'error');
        }
    });
}

function validateForm() {
    let isValid = true;

    const firstNameInput = document.getElementById('profile-first-name');
    const lastNameInput = document.getElementById('profile-last-name');
    const passwordInput = document.getElementById('profile-password');
    const confirmInput = document.getElementById('profile-confirm-password');

    const firstNameError = document.getElementById('profile-first-name-error');
    const lastNameError = document.getElementById('profile-last-name-error');
    const passwordError = document.getElementById('profile-password-error');
    const confirmError = document.getElementById('profile-confirm-error');

    
    firstNameInput.classList.remove('is-invalid');
    lastNameInput.classList.remove('is-invalid');
    passwordInput.classList.remove('is-invalid');
    confirmInput.classList.remove('is-invalid');

    firstNameError.style.display = 'none';
    lastNameError.style.display = 'none';
    passwordError.style.display = 'none';
    confirmError.style.display = 'none';

    
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

    
    const newPassword = passwordInput.value;
    if (newPassword) {
        if (newPassword.length < 6 || newPassword.length > 100) {
            passwordInput.classList.add('is-invalid');
            passwordError.style.display = 'block';
            isValid = false;
        }

        
        if (newPassword !== confirmInput.value) {
            confirmInput.classList.add('is-invalid');
            confirmError.style.display = 'block';
            isValid = false;
        }
    }

    return isValid;
}
