<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Mantenimiento de Flota Vehicular - Acceso al Sistema</title>
    <!-- Fuentes y estilos de Tailwind CSS -->
    <link href="https://fonts.googleapis.com" rel="preconnect"/>
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@500&family=Plus+Jakarta+Sans:wght@400;600;700&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet"/>
    <style>@layer base{html,body{margin:0;padding:0;}body{overscroll-behavior:none;}}::-webkit-scrollbar{display:none;}</style>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-50 font-sans text-slate-900 min-h-screen flex flex-col justify-between">
    <main class="w-full flex-1 flex flex-col items-center justify-center relative px-4 py-12">
        <div class="w-full max-w-[480px] bg-white rounded-xl shadow-xl p-8 relative z-10 border border-slate-100">
            <div class="flex flex-col items-center text-center">
                <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-50 text-blue-700 font-bold text-xs uppercase tracking-wider mb-4">
                    <span class="material-symbols-outlined text-[14px]">local_shipping</span>
                    <span>Mantenimiento · Telemetría</span>
                </div>
                <h1 class="text-2xl font-bold text-slate-900">Acceso al Sistema</h1>
                <p class="text-sm text-slate-600 mt-1">Plataforma de Control y Gestión de Flota Vehicular</p>
            </div>

            <!-- Alerta dinámica conectada con el Servlet (Manejo de errores y 3 intentos) -->
            <% 
                String error = (String) request.getAttribute("error");
                String hiddenClass = (error != null) ? "" : "hidden";
            %>
            <div class="mt-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3 <%= hiddenClass %>" id="authAlert">
                <span class="material-symbols-outlined text-red-600 text-[20px] mt-0.5 shrink-0">error</span>
                <div class="flex-1 min-w-0">
                    <p class="font-bold text-sm text-red-700">Aviso de seguridad</p>
                    <p class="text-xs text-red-600 mt-1"><%= (error != null) ? error : "" %></p>
                </div>
                <button class="text-red-400 hover:text-red-700 p-1 rounded-md transition-colors" onclick="document.getElementById('authAlert').classList.add('hidden')" type="button">
                    <span class="material-symbols-outlined text-[18px]">close</span>
                </button>
            </div>

            <!-- Formulario apuntando al LoginServlet y con los names requeridos por Java -->
            <form action="LoginServlet" method="POST" class="mt-6 flex flex-col gap-4">
                <div>
                    <label class="flex items-center justify-between text-xs font-bold text-slate-700 mb-1" for="username">
                        <span>Usuario <span class="text-red-600">*</span></span>
                    </label>
                    <div class="relative flex items-center">
                        <span class="material-symbols-outlined absolute left-3 text-slate-400 text-[20px]">badge</span>
                        <input autocomplete="username" class="w-full h-11 pl-11 pr-4 bg-slate-50 border border-slate-200 text-sm text-slate-900 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 transition-all" id="username" name="txtUsuario" placeholder="ej. admin" required type="text"/>
                    </div>
                </div>
                
                <div>
                    <label class="flex items-center justify-between text-xs font-bold text-slate-700 mb-1" for="password">
                        <span>Contraseña <span class="text-red-600">*</span></span>
                    </label>
                    <div class="relative flex items-center">
                        <span class="material-symbols-outlined absolute left-3 text-slate-400 text-[20px]">lock</span>
                        <input autocomplete="current-password" class="w-full h-11 pl-11 pr-11 bg-slate-50 border border-slate-200 text-sm text-slate-900 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 transition-all" id="password" name="txtPassword" placeholder="••••••••" required type="password"/>
                        <button aria-label="Mostrar u ocultar contraseña" class="absolute right-3 p-1 text-slate-400 hover:text-slate-700 transition-colors" id="togglePassword" type="button">
                            <span class="material-symbols-outlined text-[20px]" id="eyeIcon">visibility</span>
                        </button>
                    </div>
                </div>

                <button class="w-full h-11 mt-2 bg-blue-600 hover:bg-blue-700 active:scale-[0.99] text-white font-bold text-sm rounded-lg shadow-md transition-all flex items-center justify-center gap-2 cursor-pointer" type="submit">
                    <span>Ingresar al Portal</span>
                    <span class="material-symbols-outlined text-[18px]">arrow_forward</span>
                </button>
            </form>
        </div>
    </main>

    <script>
        // Script para mostrar/ocultar contraseña
        (function() {
            const passwordInput = document.getElementById('password');
            const toggleButton = document.getElementById('togglePassword');
            const eyeIcon = document.getElementById('eyeIcon');

            if (passwordInput && toggleButton && eyeIcon) {
                toggleButton.addEventListener('click', function() {
                    const isPassword = passwordInput.getAttribute('type') === 'password';
                    passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
                    eyeIcon.textContent = isPassword ? 'visibility_off' : 'visibility';
                });
            }
        })();
    </script>
</body>
</html>