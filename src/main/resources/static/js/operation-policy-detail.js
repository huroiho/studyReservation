// ========================================
// Delete Modal
// ========================================
function openDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }
}

// ========================================
// Confirm Dialog
// ========================================
function openConfirmDialog() {
    const dialog = document.getElementById('confirmDialog');
    if (dialog) {
        dialog.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeConfirmDialog() {
    const dialog = document.getElementById('confirmDialog');
    if (dialog) {
        dialog.style.display = 'none';
        document.body.style.overflow = '';
    }
}

// ========================================
// Toggle Status Handler
// ========================================
function handleToggleClick(event) {
    event.preventDefault();
    const button = event.currentTarget;
    const isActive = button.getAttribute('data-active') === 'true';
    const hasRooms = true; // This would come from server-side in real Thymeleaf
    
    // If deactivating and has rooms, show confirmation
    if (isActive && hasRooms) {
        openConfirmDialog();
    } else {
        // Otherwise, submit the form directly
        submitToggleForm();
    }
}

function confirmToggleStatus() {
    closeConfirmDialog();
    submitToggleForm();
}

function submitToggleForm() {
    const form = document.getElementById('statusToggleForm');
    if (form) {
        // In a real implementation, this would submit to the server
        // For now, we'll just simulate the toggle visually
        const button = form.querySelector('.toggle-switch');
        const statusText = form.parentElement.querySelector('.status-text');
        const hiddenInput = form.querySelector('input[name="isActive"]');
        
        if (button && statusText && hiddenInput) {
            const currentActive = button.classList.contains('active');
            
            if (currentActive) {
                button.classList.remove('active');
                statusText.classList.remove('active');
                statusText.textContent = 'INACTIVE';
                hiddenInput.value = 'false';
            } else {
                button.classList.add('active');
                statusText.classList.add('active');
                statusText.textContent = 'ACTIVE';
                hiddenInput.value = 'true';
            }
            
            button.setAttribute('data-active', (!currentActive).toString());
            
            // In production, submit the form:
            // form.submit();
            
            // Show success message (you can integrate with your toast library)
            console.log('정책 상태가 변경되었습니다.');
        }
    }
}

// ========================================
// Keyboard Event Handlers
// ========================================
document.addEventListener('DOMContentLoaded', function() {
    // Close modals on Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeDeleteModal();
            closeConfirmDialog();
        }
    });
});

// ========================================
// Form Submission Interceptors (Optional)
// ========================================
// If you need to add validation or confirmation before form submission:
/*
document.addEventListener('DOMContentLoaded', function() {
    const deleteForm = document.querySelector('form[action*="/delete"]');
    if (deleteForm) {
        deleteForm.addEventListener('submit', function(e) {
            // Add additional validation if needed
            const confirmed = confirm('정말 삭제하시겠습니까?');
            if (!confirmed) {
                e.preventDefault();
            }
        });
    }
});
*/
