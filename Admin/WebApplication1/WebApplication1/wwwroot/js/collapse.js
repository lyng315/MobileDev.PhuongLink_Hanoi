// bật/tắt .collapse và đổi icon
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.btn-toggle').forEach(btn => {
        btn.addEventListener('click', () => {
            const target = document.querySelector(btn.getAttribute('data-target'));
            if (!target) return;

            const icon = btn.querySelector('i');
            if (getComputedStyle(target).display === 'block') {
                target.style.display = 'none';
                if (icon) {
                    icon.classList.replace('bi-chevron-up', 'bi-chevron-down');
                }
            } else {
                target.style.display = 'block';
                if (icon) {
                    icon.classList.replace('bi-chevron-down', 'bi-chevron-up');
                }
            }
        });
    });
});
