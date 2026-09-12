// ═══════════════════════════════════════════
// APEX MONEY — Scroll Reveal & Interactions
// ═══════════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {

  // ── Scroll Reveal ──
  const reveals = document.querySelectorAll('.reveal');

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
      }
    });
  }, {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  });

  reveals.forEach(el => observer.observe(el));

  // ── Nav background on scroll ──
  const nav = document.getElementById('nav');
  let lastScroll = 0;

  window.addEventListener('scroll', () => {
    const currentScroll = window.pageYOffset;

    if (currentScroll > 100) {
      nav.style.borderBottomColor = 'rgba(255, 42, 42, 0.15)';
      nav.style.background = 'rgba(5, 5, 5, 0.9)';
    } else {
      nav.style.borderBottomColor = 'rgba(74, 21, 21, 0.2)';
      nav.style.background = 'rgba(5, 5, 5, 0.7)';
    }

    lastScroll = currentScroll;
  }, { passive: true });

  // ── Smooth scroll for nav links ──
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', (e) => {
      const target = document.querySelector(anchor.getAttribute('href'));
      if (target) {
        e.preventDefault();
        target.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  });

});
