/* ============================================================
   SmartStay — Shared JavaScript
   ============================================================ */

document.addEventListener('DOMContentLoaded', function() {
  initTheme();
  initSidebar();
  initRipple();
  initCountUp();
  initAccordion();
  initModal();
  initMobileNav();
});

/* ============================================================
   Theme Toggle (Dark/Light)
   ============================================================ */
function initTheme() {
  var stored = localStorage.getItem('smartstay-theme');
  if (stored) {
    document.documentElement.setAttribute('data-theme', stored);
  }

  document.querySelectorAll('.theme-toggle').forEach(function(btn) {
    btn.addEventListener('click', function() {
      var current = document.documentElement.getAttribute('data-theme');
      var next = current === 'light' ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', next);
      localStorage.setItem('smartstay-theme', next);
    });
  });
}

/* ============================================================
   Sidebar Toggle (Mobile)
   ============================================================ */
function initSidebar() {
  var hamburger = document.querySelector('.hamburger');
  var sidebar = document.querySelector('.sidebar');
  var overlay = document.querySelector('.sidebar-overlay');

  if (!hamburger || !sidebar) return;

  hamburger.addEventListener('click', function() {
    sidebar.classList.toggle('open');
    if (overlay) overlay.classList.toggle('active');
  });

  if (overlay) {
    overlay.addEventListener('click', function() {
      sidebar.classList.remove('open');
      overlay.classList.remove('active');
    });
  }
}

/* ============================================================
   Ripple Effect on Buttons
   ============================================================ */
function initRipple() {
  document.querySelectorAll('.btn').forEach(function(btn) {
    btn.addEventListener('click', function(e) {
      var rect = btn.getBoundingClientRect();
      var ripple = document.createElement('span');
      ripple.className = 'ripple';
      var size = Math.max(rect.width, rect.height);
      ripple.style.width = ripple.style.height = size + 'px';
      ripple.style.left = (e.clientX - rect.left - size / 2) + 'px';
      ripple.style.top = (e.clientY - rect.top - size / 2) + 'px';
      btn.appendChild(ripple);
      setTimeout(function() { ripple.remove(); }, 600);
    });
  });
}

/* ============================================================
   Count-Up Animation
   ============================================================ */
function initCountUp() {
  var counters = document.querySelectorAll('.count-up');
  if (!counters.length) return;

  var observer = new IntersectionObserver(function(entries) {
    entries.forEach(function(entry) {
      if (entry.isIntersecting) {
        animateCounter(entry.target);
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.5 });

  counters.forEach(function(counter) { observer.observe(counter); });
}

function animateCounter(el) {
  var target = parseFloat(el.getAttribute('data-target'));
  if (isNaN(target)) return;

  var prefix = el.getAttribute('data-prefix') || '';
  var suffix = el.getAttribute('data-suffix') || '';
  var decimals = parseInt(el.getAttribute('data-decimals') || '0', 10);
  var duration = 1500;
  var start = 0;
  var startTime = null;

  function step(timestamp) {
    if (!startTime) startTime = timestamp;
    var progress = Math.min((timestamp - startTime) / duration, 1);
    var eased = 1 - Math.pow(1 - progress, 3); // ease-out cubic
    var current = start + (target - start) * eased;
    el.textContent = prefix + current.toFixed(decimals) + suffix;
    if (progress < 1) {
      requestAnimationFrame(step);
    }
  }

  requestAnimationFrame(step);
}

/* ============================================================
   Accordion
   ============================================================ */
function initAccordion() {
  document.querySelectorAll('.accordion-header').forEach(function(header) {
    header.addEventListener('click', function() {
      var item = header.parentElement;
      var wasOpen = item.classList.contains('open');

      // Close all siblings
      item.parentElement.querySelectorAll('.accordion-item').forEach(function(sibling) {
        sibling.classList.remove('open');
      });

      if (!wasOpen) {
        item.classList.add('open');
      }
    });
  });
}

/* ============================================================
   Modal
   ============================================================ */
function initModal() {
  // Open modal
  document.querySelectorAll('[data-modal-target]').forEach(function(trigger) {
    trigger.addEventListener('click', function() {
      var targetId = trigger.getAttribute('data-modal-target');
      var modal = document.getElementById(targetId);
      if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
      }
    });
  });

  // Close modal
  document.querySelectorAll('.modal-overlay').forEach(function(overlay) {
    overlay.addEventListener('click', function(e) {
      if (e.target === overlay) {
        overlay.classList.remove('active');
        document.body.style.overflow = '';
      }
    });

    var closeBtn = overlay.querySelector('.modal-close');
    if (closeBtn) {
      closeBtn.addEventListener('click', function() {
        overlay.classList.remove('active');
        document.body.style.overflow = '';
      });
    }
  });
}

function openModal(id) {
  var modal = document.getElementById(id);
  if (modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
  }
}

function closeModal(id) {
  var modal = document.getElementById(id);
  if (modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
  }
}

/* ============================================================
   Mobile Bottom Nav Active State
   ============================================================ */
function initMobileNav() {
  var currentPage = window.location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('.bottom-nav a').forEach(function(link) {
    var href = link.getAttribute('href');
    if (href === currentPage || (currentPage === '' && href === 'index.html')) {
      link.classList.add('active');
    }
  });
}

/* ============================================================
   Kanban Tab Switching (Mobile)
   ============================================================ */
function initKanbanTabs() {
  document.querySelectorAll('.kanban-tab').forEach(function(tab) {
    tab.addEventListener('click', function() {
      var target = tab.getAttribute('data-tab');

      document.querySelectorAll('.kanban-tab').forEach(function(t) {
        t.classList.remove('active');
      });
      tab.classList.add('active');

      document.querySelectorAll('.kanban-column').forEach(function(col) {
        col.classList.remove('active');
      });
      document.querySelector('.kanban-column[data-column="' + target + '"]').classList.add('active');
    });
  });
}

/* ============================================================
   Chart.js Placeholder Initialization
   ============================================================ */
function initCharts() {
  if (typeof Chart === 'undefined') return;

  Chart.defaults.color = '#8b949e';
  Chart.defaults.borderColor = 'rgba(48, 54, 61, 0.5)';
  Chart.defaults.font.family = "'Inter', sans-serif";

  // Initialize any canvas with data-chart-type attribute
  document.querySelectorAll('canvas[data-chart-type]').forEach(function(canvas) {
    var type = canvas.getAttribute('data-chart-type');
    var ctx = canvas.getContext('2d');

    var config = getChartConfig(type, canvas);
    if (config) {
      new Chart(ctx, config);
    }
  });
}

function getChartConfig(type, canvas) {
  var configs = {
    doughnut: {
      type: 'doughnut',
      data: {
        labels: ['Sample A', 'Sample B', 'Sample C'],
        datasets: [{
          data: [40, 35, 25],
          backgroundColor: ['#1a2a6c', '#c9a227', '#2ea043'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '65%',
        plugins: {
          legend: { position: 'bottom', labels: { padding: 16, usePointStyle: true } }
        }
      }
    },
    bar: {
      type: 'bar',
      data: {
        labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        datasets: [{
          label: 'Revenue',
          data: [1200, 1900, 1500, 2100, 1800, 2400, 2200],
          backgroundColor: '#c9a227',
          borderRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: { beginAtZero: true, grid: { color: 'rgba(48,54,61,0.3)' } },
          x: { grid: { display: false } }
        }
      }
    },
    line: {
      type: 'line',
      data: {
        labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4'],
        datasets: [{
          label: 'Trend',
          data: [65, 78, 72, 85],
          borderColor: '#c9a227',
          backgroundColor: 'rgba(201,162,39,0.1)',
          fill: true,
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: { beginAtZero: true, grid: { color: 'rgba(48,54,61,0.3)' } },
          x: { grid: { display: false } }
        }
      }
    }
  };

  return configs[type] || null;
}

// Run chart init after Chart.js loads
if (typeof Chart !== 'undefined') {
  initCharts();
} else {
  window.addEventListener('load', function() {
    if (typeof Chart !== 'undefined') initCharts();
  });
}

/* ============================================================
   AI Assistant (Guest Portal)
   ============================================================ */
function initAIAssistant() {
  var openBtn = document.querySelector('[data-ai-open]');
  var overlay = document.querySelector('.ai-assistant-overlay');
  var closeBtn = overlay ? overlay.querySelector('.ai-assistant-close') : null;
  var sendBtn = overlay ? overlay.querySelector('.ai-send-btn') : null;
  var input = overlay ? overlay.querySelector('.ai-chat-input') : null;
  var chatBody = overlay ? overlay.querySelector('.ai-assistant-body') : null;

  if (!openBtn || !overlay) return;

  // Open
  openBtn.addEventListener('click', function() {
    overlay.classList.add('active');
    document.body.style.overflow = 'hidden';
    if (input) input.focus();
  });

  // Close
  function closeAI() {
    overlay.classList.remove('active');
    document.body.style.overflow = '';
  }

  if (closeBtn) closeBtn.addEventListener('click', closeAI);
  overlay.addEventListener('click', function(e) {
    if (e.target === overlay) closeAI();
  });

  // Quick actions
  overlay.querySelectorAll('.ai-quick-action').forEach(function(chip) {
    chip.addEventListener('click', function() {
      var text = chip.getAttribute('data-prompt') || chip.textContent.trim();
      sendAIMessage(text, chatBody);
    });
  });

  // Send
  if (sendBtn && input) {
    sendBtn.addEventListener('click', function() {
      var text = input.value.trim();
      if (text) {
        sendAIMessage(text, chatBody);
        input.value = '';
      }
    });

    input.addEventListener('keydown', function(e) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendBtn.click();
      }
    });
  }
}

function sendAIMessage(text, chatBody) {
  if (!chatBody) return;

  // Add user message
  var userMsg = document.createElement('div');
  userMsg.className = 'chat-message user';
  userMsg.textContent = text;
  chatBody.appendChild(userMsg);

  // Show typing indicator
  var typing = document.createElement('div');
  typing.className = 'ai-typing';
  typing.innerHTML = '<span class="dot"></span><span class="dot"></span><span class="dot"></span>';
  chatBody.appendChild(typing);
  chatBody.scrollTop = chatBody.scrollHeight;

  // Simulate AI response
  setTimeout(function() {
    typing.remove();
    var aiMsg = document.createElement('div');
    aiMsg.className = 'chat-message ai';
    aiMsg.innerHTML = getAIResponse(text);
    chatBody.appendChild(aiMsg);
    chatBody.scrollTop = chatBody.scrollHeight;
  }, 1200 + Math.random() * 800);
}

function getAIResponse(input) {
  var lower = input.toLowerCase();

  // Food ordering
  if (lower.includes('order food') || lower.includes('hungry') || lower.includes('food') || lower.includes('menu')) {
    return 'I can help you order food! 🍽️<br><br><strong>Popular items:</strong><br>• Butter Chicken — ₹380<br>• Paneer Tikka — ₹320<br>• Chicken Biryani — ₹420<br>• Dal Makhani — ₹260<br><br>Tap <em>"Order Food"</em> from the home screen, or tell me what you\'d like and I\'ll add it to your cart.';
  }

  // Bill inquiry
  if (lower.includes('bill') || lower.includes('invoice') || lower.includes('charge') || lower.includes('payment')) {
    return 'Here\'s your current billing summary:<br><br>• Room charges (3 nights): ₹18,000<br>• Food orders: ₹2,450<br>• GST (12% rooms): ₹2,160<br>• GST (5% food): ₹122<br><br><strong>Grand Total: ₹22,732</strong><br><br>Tap <em>"My Bill"</em> for the full itemized breakdown.';
  }

  // Room service / housekeeping
  if (lower.includes('housekeeping') || lower.includes('clean') || lower.includes('towel') || lower.includes('tissue')) {
    return 'I\'ve noted your housekeeping request. 🧹<br><br>A team member will be at Room 301 within 15 minutes. Is there anything specific you need?<br><br>• Fresh towels<br>• Extra pillows<br>• Room cleaning<br>• Toiletries refill';
  }

  // Checkout
  if (lower.includes('checkout') || lower.includes('check out') || lower.includes('leave') || lower.includes('depart')) {
    return 'Your checkout is scheduled for <strong>28 May 2026, 11:00 AM</strong>.<br><br>• Late checkout available until 2 PM (₹1,500)<br>• Express checkout via the app<br>• Luggage storage available after checkout<br><br>Tap <em>"Check Out"</em> to start the process now.';
  }

  // WiFi
  if (lower.includes('wifi') || lower.includes('internet') || lower.includes('network')) {
    return 'WiFi Details:<br><br>• Network: <strong>SmartStay_Guest</strong><br>• Password: <strong>Welcome2026</strong><br>• Speed: 100 Mbps<br><br>Having trouble connecting? I can restart the router for your room.';
  }

  // General help
  if (lower.includes('help') || lower.includes('what can') || lower.includes('how')) {
    return 'I\'m your SmartStay AI assistant! I can help with:<br><br>🍽️ <strong>Order food</strong> — Browse menu or order directly<br>📋 <strong>View bill</strong> — Check charges and payments<br>🧹 <strong>Housekeeping</strong> — Request cleaning or supplies<br>🚗 <strong>Transport</strong> — Book taxi or airport transfer<br>📶 <strong>WiFi</strong> — Get connection details<br>🏨 <strong>Hotel info</strong> — Spa, gym, pool hours<br><br>Just ask!';
  }

  // Spa / gym / pool
  if (lower.includes('spa') || lower.includes('gym') || lower.includes('pool') || lower.includes('fitness')) {
    return 'Hotel Facilities:<br><br>🏊 <strong>Pool:</strong> 6 AM – 10 PM (Rooftop, Level 8)<br>🏋️ <strong>Gym:</strong> 24/7 (Level 2)<br>💆 <strong>Spa:</strong> 9 AM – 9 PM (Level 3)<br><br>Spa appointments can be booked at the front desk or I can check availability for you.';
  }

  // Default
  var defaults = [
    'I\'m here to help! Could you tell me more about what you need? I can assist with food orders, billing, housekeeping, hotel facilities, and more.',
    'Got it! Let me look into that for you. In the meantime, is there anything else I can help with — food, billing, or room service?',
    'I understand. I\'ll make a note of that. For immediate assistance, you can also call reception at ext. 0.'
  ];
  return defaults[Math.floor(Math.random() * defaults.length)];
}

// Run AI assistant init
document.addEventListener('DOMContentLoaded', function() {
  initAIAssistant();
});
