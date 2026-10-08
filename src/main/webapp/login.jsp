<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // Mensaje de error enviado por el LoginServlet (si existe)
    String error = (String) request.getAttribute("error");
    String hiddenClass = (error != null) ? "" : "hidden";
%>
<!DOCTYPE html>
<html lang="es" class="h-full bg-slate-50">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FleetPro – Control & Mantenimiento de Flota Vehicular</title>
  <!-- Google Fonts: Plus Jakarta Sans & Material Symbols Outlined -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,300;0,400;0,500;0,600;0,700;0,800;1,400&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet">
  <!-- Tailwind CSS CDN -->
  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    tailwind.config = {
      theme: {
        extend: {
          fontFamily: {
            sans: ['"Plus Jakarta Sans"', 'sans-serif'],
          },
          colors: {
            navy: {
              900: '#0F172A',
              950: '#090D16',
            },
            fleet: {
              primary: '#2563EB',
              indigo: '#312E81',
              accent: '#F97316',
              success: '#10B981',
              danger: '#DC2626',
            }
          },
          animation: {
            'fade-in-up': 'fadeInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards',
          },
          keyframes: {
            fadeInUp: {
              '0%': { opacity: '0', transform: 'translateY(16px)' },
              '100%': { opacity: '1', transform: 'translateY(0)' },
            }
          }
        }
      }
    }
  </script>
  <style>
    .material-symbols-outlined {
      font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
      display: inline-block;
      vertical-align: middle;
      line-height: 1;
    }
    .road-grid-pattern {
      background-image:
        linear-gradient(to right, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
        linear-gradient(to bottom, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
      background-size: 36px 36px;
    }
    @media (prefers-reduced-motion: reduce) {
      .animate-fade-in-up, .animate-ping, .animate-pulse { animation: none !important; opacity: 1 !important; }
    }
  </style>
</head>
<body class="h-full font-sans antialiased text-slate-800 bg-[#F8FAFC] selection:bg-fleet-primary selection:text-white">

  <div class="min-h-full flex flex-col lg:flex-row">

    <!-- ========== COLUMNA IZQUIERDA: marca ========== -->
    <section class="relative lg:w-[55%] bg-gradient-to-br from-[#0F172A] via-[#1E1B4B] to-[#312E81] text-white p-6 sm:p-10 lg:p-14 xl:p-16 flex flex-col justify-between overflow-hidden shadow-2xl lg:shadow-none z-10">

      <!-- Fondos decorativos -->
      <div class="absolute inset-0 pointer-events-none overflow-hidden opacity-30">
        <div class="absolute -top-32 -left-32 w-96 h-96 rounded-full bg-blue-500/20 blur-3xl"></div>
        <div class="absolute top-1/2 -right-24 w-80 h-80 rounded-full bg-orange-500/15 blur-3xl"></div>
        <div class="absolute -bottom-24 left-1/4 w-96 h-96 rounded-full bg-indigo-500/30 blur-3xl"></div>
        <div class="absolute inset-0 road-grid-pattern opacity-40"></div>

        <svg class="absolute inset-0 w-full h-full" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="none" viewBox="0 0 600 800" fill="none">
          <path d="M-100 700 C 150 650, 200 450, 450 380 C 580 340, 650 200, 700 100" stroke="url(#routeGrad1)" stroke-width="2.5" stroke-dasharray="8 6" opacity="0.45" />
          <path d="M-50 780 C 180 720, 260 520, 520 440 C 640 400, 700 250, 750 160" stroke="#2563EB" stroke-width="1.5" opacity="0.3" />
          <path d="M50 820 C 220 760, 320 600, 580 500" stroke="#F97316" stroke-width="2" stroke-dasharray="4 6" opacity="0.35" />
          <circle cx="200" cy="450" r="5" fill="#2563EB" opacity="0.7"/>
          <circle cx="200" cy="450" r="11" stroke="#2563EB" stroke-width="1.5" opacity="0.3" class="animate-ping"/>
          <circle cx="450" cy="380" r="4" fill="#F97316" opacity="0.8"/>
          <defs>
            <linearGradient id="routeGrad1" x1="0%" y1="100%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#2563EB" />
              <stop offset="60%" stop-color="#38BDF8" />
              <stop offset="100%" stop-color="#F97316" />
            </linearGradient>
          </defs>
        </svg>
      </div>

      <!-- Cabecera: logotipo + ubicación -->
      <div class="relative z-10 flex items-center justify-between">
        <div class="flex items-center gap-3.5">
          <div class="w-12 h-12 rounded-2xl bg-gradient-to-br from-[#2563EB] via-[#3B82F6] to-[#F97316] p-0.5 shadow-lg shadow-blue-900/50 flex items-center justify-center transition-transform hover:scale-105 duration-300">
            <div class="w-full h-full bg-[#0F172A]/85 backdrop-blur-sm rounded-[14px] flex items-center justify-center text-white">
              <svg class="w-6 h-6 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="1" y="4" width="14" height="11" rx="2" />
                <path d="M15 8h4.5a2 2 0 0 1 1.7 1l1.8 3v3h-8V8z" />
                <circle cx="6" cy="18" r="2.5" fill="#F97316" stroke="none" />
                <circle cx="17" cy="18" r="2.5" fill="#2563EB" stroke="none" />
                <path d="M6 8h4" stroke="#38BDF8" stroke-width="1.5"/>
              </svg>
            </div>
          </div>
          <div>
            <div class="flex items-center gap-1.5">
              <span class="text-xl font-extrabold tracking-tight text-white">Fleet<span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-orange-400">Pro</span></span>
            </div>
            <p class="text-[11px] font-medium text-slate-400 tracking-wide">Control & Mantenimiento de Flota</p>
          </div>
        </div>

        <div class="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-800/60 border border-slate-700/60 backdrop-blur-sm text-xs text-slate-300">
          <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
          <span class="font-medium text-slate-200">Lima, Perú</span>
        </div>
      </div>

      <!-- Propuesta de valor -->
      <div class="relative z-10 my-8 lg:my-auto py-2">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-gradient-to-r from-blue-500/20 to-orange-500/20 border border-blue-400/25 text-xs font-semibold text-blue-300 mb-4 backdrop-blur-sm">
          <span class="material-symbols-outlined text-[15px] text-orange-400">precision_manufacturing</span>
          <span>Motor de reglas de mantenimiento preventivo</span>
        </div>

        <h1 class="text-3xl sm:text-4xl lg:text-5xl font-extrabold text-white tracking-tight leading-[1.15] max-w-xl">
          Mantenimiento preventivo, <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 via-sky-300 to-orange-400">sin sorpresas</span>
        </h1>

        <p class="mt-4 text-base sm:text-lg text-slate-300/90 leading-relaxed max-w-lg font-normal">
          Controla el desgaste de tu flota y anticipa las fallas antes de que detengan tu operación.
        </p>

        <!-- Ilustración del camión (vista de ejemplo) -->
        <div class="relative mt-8 max-w-lg hidden sm:block">
          <div class="relative rounded-2xl bg-gradient-to-b from-slate-800/60 to-slate-900/80 border border-slate-700/50 p-5 backdrop-blur-md overflow-hidden shadow-xl">
            <div class="absolute -bottom-8 left-1/4 right-1/4 h-16 bg-blue-500/20 blur-xl rounded-full"></div>

            <div class="flex items-center justify-between mb-3 text-xs text-slate-400 border-b border-slate-700/60 pb-2.5">
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
                <span class="font-mono text-slate-300 font-semibold tracking-wider">UNIDAD T-84 [VOLVO FH-540]</span>
              </div>
              <span class="text-slate-400 font-medium">Vista de ejemplo</span>
            </div>

            <div class="relative flex items-center justify-center py-2">
              <svg class="w-full max-w-[340px] h-28 text-white filter drop-shadow-md" viewBox="0 0 360 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                <line x1="10" y1="106" x2="350" y2="106" stroke="#334155" stroke-width="2" />
                <line x1="20" y1="110" x2="60" y2="110" stroke="#F97316" stroke-width="2.5" stroke-linecap="round" />
                <line x1="100" y1="110" x2="160" y2="110" stroke="#F97316" stroke-width="2.5" stroke-linecap="round" />
                <line x1="200" y1="110" x2="260" y2="110" stroke="#F97316" stroke-width="2.5" stroke-linecap="round" />
                <line x1="300" y1="110" x2="340" y2="110" stroke="#F97316" stroke-width="2.5" stroke-linecap="round" />

                <rect x="25" y="24" width="190" height="66" rx="4" fill="#1E293B" stroke="#475569" stroke-width="2"/>
                <line x1="25" y1="46" x2="215" y2="46" stroke="#334155" stroke-width="1.5"/>
                <line x1="25" y1="68" x2="215" y2="68" stroke="#334155" stroke-width="1.5"/>
                <line x1="60" y1="24" x2="60" y2="90" stroke="#334155" stroke-width="1"/>
                <line x1="100" y1="24" x2="100" y2="90" stroke="#334155" stroke-width="1"/>
                <line x1="140" y1="24" x2="140" y2="90" stroke="#334155" stroke-width="1"/>
                <line x1="180" y1="24" x2="180" y2="90" stroke="#334155" stroke-width="1"/>

                <rect x="40" y="32" width="70" height="20" rx="3" fill="#0F172A" />
                <text x="48" y="46" fill="#38BDF8" font-size="9" font-weight="bold" font-family="'Plus Jakarta Sans', sans-serif">FLEETPRO</text>
                <text x="96" y="46" fill="#F97316" font-size="9" font-weight="bold" font-family="'Plus Jakarta Sans', sans-serif">LOG</text>

                <rect x="215" y="70" width="16" height="12" fill="#475569" />

                <path d="M228 90 L228 36 L270 20 L298 20 L322 55 L328 72 L328 90 Z" fill="#2563EB" stroke="#60A5FA" stroke-width="1.5"/>
                <path d="M272 26 L295 26 L314 54 L272 54 Z" fill="#0F172A" stroke="#38BDF8" stroke-width="1.5" />
                <path d="M242 38 L266 38 L266 54 L242 54 Z" fill="#0F172A" opacity="0.8"/>
                <polygon points="328,75 352,78 352,86 328,84" fill="#FEF08A" opacity="0.8"/>
                <line x1="328" y1="80" x2="355" y2="80" stroke="#FBBF24" stroke-width="2"/>
                <rect x="314" y="66" width="14" height="24" rx="2" fill="#1E293B" stroke="#64748B" stroke-width="1"/>
                <line x1="316" y1="72" x2="326" y2="72" stroke="#94A3B8" stroke-width="1"/>
                <line x1="316" y1="78" x2="326" y2="78" stroke="#94A3B8" stroke-width="1"/>
                <line x1="316" y1="84" x2="326" y2="84" stroke="#94A3B8" stroke-width="1"/>

                <g>
                  <circle cx="55" cy="98" r="13" fill="#090D16" stroke="#475569" stroke-width="2"/>
                  <circle cx="55" cy="98" r="6" fill="#334155" />
                  <circle cx="95" cy="98" r="13" fill="#090D16" stroke="#475569" stroke-width="2"/>
                  <circle cx="95" cy="98" r="6" fill="#334155" />
                  <circle cx="135" cy="98" r="13" fill="#090D16" stroke="#475569" stroke-width="2"/>
                  <circle cx="135" cy="98" r="6" fill="#334155" />
                </g>

                <g>
                  <circle cx="250" cy="98" r="13" fill="#090D16" stroke="#F97316" stroke-width="1.8"/>
                  <circle cx="250" cy="98" r="6" fill="#475569" />
                  <circle cx="308" cy="98" r="13" fill="#090D16" stroke="#2563EB" stroke-width="2"/>
                  <circle cx="308" cy="98" r="6" fill="#334155" />
                </g>

                <circle cx="250" cy="98" r="18" stroke="#F97316" stroke-width="1" stroke-dasharray="2 3" opacity="0.8" class="animate-pulse" />
              </svg>
            </div>

            <div class="grid grid-cols-3 gap-2 mt-2 pt-2 border-t border-slate-700/50 text-[11px]">
              <div class="bg-slate-900/70 rounded-lg p-2 border border-slate-700/40">
                <span class="text-slate-400 block text-[10px]">Neumáticos</span>
                <span class="font-bold text-emerald-400 flex items-center gap-1 mt-0.5">
                  <span class="material-symbols-outlined text-[13px]">check_circle</span> Óptimo
                </span>
              </div>
              <div class="bg-slate-900/70 rounded-lg p-2 border border-slate-700/40">
                <span class="text-slate-400 block text-[10px]">Aceite de motor</span>
                <span class="font-bold text-sky-400 flex items-center gap-1 mt-0.5">
                  <span class="material-symbols-outlined text-[13px]">oil_barrel</span> Vida útil 82%
                </span>
              </div>
              <div class="bg-slate-900/70 rounded-lg p-2 border border-slate-700/40">
                <span class="text-slate-400 block text-[10px]">Frenos</span>
                <span class="font-bold text-orange-400 flex items-center gap-1 mt-0.5">
                  <span class="material-symbols-outlined text-[13px]">build_circle</span> Revisión próxima
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Tres insignias -->
        <div class="mt-8 grid grid-cols-1 sm:grid-cols-3 gap-3 max-w-xl">
          <div class="flex items-center gap-3 p-3 rounded-xl bg-white/[0.06] border border-white/10 backdrop-blur-sm hover:bg-white/[0.1] transition-colors">
            <div class="w-9 h-9 rounded-lg bg-orange-500/20 text-orange-400 flex items-center justify-center shrink-0 border border-orange-500/30">
              <span class="material-symbols-outlined text-[20px]">notifications_active</span>
            </div>
            <div>
              <span class="text-xs font-semibold text-white block">Alertas preventivas</span>
              <span class="text-[10px] text-slate-400 block leading-tight">Según umbrales de desgaste</span>
            </div>
          </div>

          <div class="flex items-center gap-3 p-3 rounded-xl bg-white/[0.06] border border-white/10 backdrop-blur-sm hover:bg-white/[0.1] transition-colors">
            <div class="w-9 h-9 rounded-lg bg-blue-500/20 text-blue-400 flex items-center justify-center shrink-0 border border-blue-500/30">
              <span class="material-symbols-outlined text-[20px]">local_shipping</span>
            </div>
            <div>
              <span class="text-xs font-semibold text-white block">Control de unidades</span>
              <span class="text-[10px] text-slate-400 block leading-tight">Camiones de carga</span>
            </div>
          </div>

          <div class="flex items-center gap-3 p-3 rounded-xl bg-white/[0.06] border border-white/10 backdrop-blur-sm hover:bg-white/[0.1] transition-colors">
            <div class="w-9 h-9 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 border border-emerald-500/30">
              <span class="material-symbols-outlined text-[20px]">query_stats</span>
            </div>
            <div>
              <span class="text-xs font-semibold text-white block">Reportes de costos</span>
              <span class="text-[10px] text-slate-400 block leading-tight">Preventivo vs. correctivo</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Pie del panel -->
      <div class="relative z-10 pt-6 mt-4 border-t border-slate-700/60 flex flex-wrap items-center justify-between text-xs text-slate-400 gap-2">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-sm text-blue-400">verified_user</span>
          <span>Mantenimiento preventivo para flotas de carga pesada</span>
        </div>
        <span class="text-slate-500 text-[11px]">© 2026 FleetPro · Proyecto académico</span>
      </div>
    </section>


    <!-- ========== COLUMNA DERECHA: acceso ========== -->
    <main class="lg:w-[45%] bg-[#F8FAFC] flex flex-col items-center justify-center p-4 sm:p-8 lg:p-12 xl:p-16 relative">

      <div class="absolute inset-0 pointer-events-none overflow-hidden opacity-60">
        <div class="absolute -top-20 -right-20 w-72 h-72 rounded-full bg-blue-100/70 blur-3xl"></div>
        <div class="absolute -bottom-20 -left-20 w-80 h-80 rounded-full bg-slate-200/50 blur-3xl"></div>
      </div>

      <div class="w-full max-w-md relative z-10 animate-fade-in-up">

        <div class="bg-white rounded-2xl shadow-xl shadow-slate-200/80 border border-slate-200/90 relative overflow-hidden transition-all duration-300">

          <div class="h-1.5 w-full bg-gradient-to-r from-fleet-primary via-indigo-600 to-fleet-accent"></div>

          <div class="p-6 sm:p-9">

            <div class="mb-7">
              <div class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 border border-blue-200/60 text-blue-700 text-xs font-semibold tracking-wide mb-3">
                <span class="material-symbols-outlined text-[15px] text-blue-600">lock</span>
                <span>Acceso seguro</span>
              </div>

              <h2 class="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                Bienvenido de nuevo
              </h2>
              <p class="mt-1.5 text-sm text-slate-500 font-normal leading-relaxed">
                Ingresa tus credenciales para acceder al panel de control.
              </p>
            </div>

            <!-- Alerta de error: se muestra solo si el LoginServlet envía un mensaje -->
            <div id="authAlert" class="<%= hiddenClass %> mb-6 rounded-xl bg-red-50 border border-red-200/80 p-4 text-sm text-red-800 transition-all duration-200" role="alert">
              <div class="flex items-start gap-3">
                <div class="w-8 h-8 rounded-lg bg-red-100 text-red-600 flex items-center justify-center shrink-0 mt-0.5">
                  <span class="material-symbols-outlined text-[20px]">shield_with_heart</span>
                </div>
                <div class="flex-1">
                  <h4 class="font-bold text-red-900 text-xs uppercase tracking-wider flex items-center gap-1.5">
                    Aviso de seguridad
                  </h4>
                  <p id="authAlertMessage" class="mt-0.5 text-xs text-red-700 leading-normal font-medium"><%= (error != null) ? error : "" %></p>
                </div>
                <button type="button" onclick="dismissAlert()" class="text-red-400 hover:text-red-700 rounded-lg p-1 transition-colors" title="Cerrar aviso" aria-label="Cerrar aviso">
                  <span class="material-symbols-outlined text-[18px]">close</span>
                </button>
              </div>
            </div>

            <!-- Formulario conectado al LoginServlet -->
            <form action="LoginServlet" method="POST" id="fleetLoginForm" class="space-y-5" onsubmit="mostrarCarga()">

              <div>
                <label for="txtUsuario" class="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                  Usuario
                </label>
                <div class="relative rounded-xl shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <span class="material-symbols-outlined text-[20px] text-slate-400 transition-colors">badge</span>
                  </div>
                  <input
                    type="text"
                    name="txtUsuario"
                    id="txtUsuario"
                    required
                    autocomplete="username"
                    placeholder="Tu nombre de usuario"
                    class="block w-full pl-11 pr-4 py-3 bg-slate-50/50 hover:bg-slate-50 focus:bg-white text-slate-900 text-sm font-medium rounded-xl border border-slate-300 focus:border-fleet-primary focus:ring-4 focus:ring-blue-500/15 outline-none transition-all duration-200 placeholder:text-slate-400 placeholder:font-normal"
                  >
                </div>
              </div>

              <div>
                <label for="txtPassword" class="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
                  Contraseña
                </label>
                <div class="relative rounded-xl shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                    <span class="material-symbols-outlined text-[20px] text-slate-400 transition-colors">lock</span>
                  </div>
                  <input
                    type="password"
                    name="txtPassword"
                    id="txtPassword"
                    required
                    autocomplete="current-password"
                    placeholder="••••••••••••"
                    class="block w-full pl-11 pr-11 py-3 bg-slate-50/50 hover:bg-slate-50 focus:bg-white text-slate-900 text-sm font-medium rounded-xl border border-slate-300 focus:border-fleet-primary focus:ring-4 focus:ring-blue-500/15 outline-none transition-all duration-200 placeholder:text-slate-400 tracking-wider"
                  >
                  <button
                    type="button"
                    id="togglePasswordBtn"
                    onclick="togglePasswordVisibility()"
                    class="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-400 hover:text-slate-600 focus:text-fleet-primary transition-colors focus:outline-none"
                    aria-label="Mostrar u ocultar contraseña"
                    title="Mostrar contraseña"
                  >
                    <span class="material-symbols-outlined text-[20px]" id="passwordIcon">visibility</span>
                  </button>
                </div>
              </div>

              <div class="flex items-center text-xs text-slate-500 pt-1">
                <span class="inline-flex items-center gap-1.5 text-slate-500">
                  <span class="material-symbols-outlined text-[16px] text-emerald-600">verified</span>
                  <span>Conexión segura (HTTPS)</span>
                </span>
              </div>

              <div class="pt-2">
                <button
                  type="submit"
                  id="btnSubmitLogin"
                  class="group relative w-full flex items-center justify-center gap-2.5 py-3.5 px-6 rounded-xl text-white font-bold text-sm tracking-wide bg-gradient-to-r from-blue-600 via-blue-700 to-indigo-800 hover:from-blue-500 hover:via-blue-600 hover:to-indigo-700 shadow-md shadow-blue-500/25 hover:shadow-lg hover:shadow-blue-500/35 hover:-translate-y-0.5 active:translate-y-0 focus:outline-none focus:ring-4 focus:ring-blue-500/30 transition-all duration-200 cursor-pointer"
                >
                  <span id="btnSubmitText">Ingresar al Portal</span>
                  <span class="material-symbols-outlined text-[20px] transition-transform duration-200 group-hover:translate-x-1" id="btnSubmitIcon">
                    arrow_forward
                  </span>
                  <svg id="btnSubmitSpinner" class="hidden animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                </button>
              </div>
            </form>

            <div class="mt-7 pt-5 border-t border-slate-100 text-center">
              <p class="text-xs text-slate-500 leading-relaxed font-normal">
                <span class="font-medium text-slate-700">Importante:</span> Si tu cuenta se bloquea tras 3 intentos fallidos, contacta al administrador.
              </p>
            </div>

          </div>
        </div>

        <div class="mt-6 flex items-center justify-between px-2 text-xs text-slate-400">
          <span>Proyecto académico · Programación Orientada a Objetos II</span>
          <span>Lima 2026</span>
        </div>

      </div>

    </main>

  </div>

  <script>
    // Mostrar u ocultar la contraseña
    function togglePasswordVisibility() {
      var passwordInput = document.getElementById('txtPassword');
      var passwordIcon = document.getElementById('passwordIcon');
      var toggleBtn = document.getElementById('togglePasswordBtn');

      if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        passwordIcon.textContent = 'visibility_off';
        toggleBtn.setAttribute('title', 'Ocultar contraseña');
      } else {
        passwordInput.type = 'password';
        passwordIcon.textContent = 'visibility';
        toggleBtn.setAttribute('title', 'Mostrar contraseña');
      }
    }

    // Cerrar el aviso de error
    function dismissAlert() {
      document.getElementById('authAlert').classList.add('hidden');
    }

    // Estado de carga del botón mientras el servlet procesa el login
    function mostrarCarga() {
      var btn = document.getElementById('btnSubmitLogin');
      document.getElementById('btnSubmitText').textContent = 'Verificando credenciales...';
      document.getElementById('btnSubmitIcon').classList.add('hidden');
      document.getElementById('btnSubmitSpinner').classList.remove('hidden');
      btn.classList.add('opacity-90', 'cursor-wait');
      // Se desactiva después de enviar el formulario para no duplicar el envío
      setTimeout(function () { btn.disabled = true; }, 0);
    }

    // Si el usuario vuelve atrás con el navegador, se restaura el botón
    window.addEventListener('pageshow', function (e) {
      if (e.persisted) {
        var btn = document.getElementById('btnSubmitLogin');
        btn.disabled = false;
        btn.classList.remove('opacity-90', 'cursor-wait');
        document.getElementById('btnSubmitText').textContent = 'Ingresar al Portal';
        document.getElementById('btnSubmitIcon').classList.remove('hidden');
        document.getElementById('btnSubmitSpinner').classList.add('hidden');
      }
    });
  </script>
</body>
</html>
