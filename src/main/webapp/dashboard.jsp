<%-- 
    Document   : dashboard
    Created on : 27 set. 2026, 5:39:50 p. m.
    Author     : saidm
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // Validar si el usuario ha iniciado sesión (Seguridad básica de sesión)
    String usuarioLogueado = (String) session.getAttribute("usuarioLogueado");
    if (usuarioLogueado == null) {
        // Si no hay sesión, redirigir al login
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>FleetPro - Panel de Control y Mantenimiento de Flota</title>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com" rel="preconnect"/>
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@500&family=Plus+Jakarta+Sans:wght@400;600;700&display=swap" rel="stylesheet"/>
    <style>@layer base{html,body{margin:0;padding:0;}body{overscroll-behavior:none;}}::-webkit-scrollbar{display:none;}</style>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-[#f8f9ff] font-sans text-slate-900 antialiased">
    
    <!-- BARRA LATERAL (SIDEBAR) -->
    <aside class="fixed left-0 top-0 h-screen w-72 bg-white shadow-md z-50 flex flex-col justify-between">
        <div class="flex flex-col">
            <div class="h-16 px-6 flex items-center gap-3">
                <div class="w-8 h-8 rounded-md bg-blue-600 flex items-center justify-center text-white font-bold">F</div>
                <div class="flex flex-col">
                    <span class="font-bold text-lg text-blue-600 tracking-tight leading-none">FleetPro</span>
                    <span class="text-xs text-slate-500 leading-none mt-1">Control & Mantenimiento</span>
                </div>
            </div>
            <div class="px-4 py-2">
                <div class="px-2 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-400">Operaciones Centrales</div>
            </div>
            <nav class="flex flex-col gap-1 px-4">
                <a class="flex items-center gap-3 px-4 py-2.5 bg-blue-600 text-white font-semibold rounded-xl shadow-sm" href="#">
                    <span class="material-symbols-outlined text-[20px]">dashboard</span><span>Inicio</span>
                </a>
                <a class="flex items-center gap-3 px-4 py-2.5 rounded-xl font-semibold text-slate-600 hover:bg-slate-100 hover:text-slate-900 transition-colors" href="#">
                    <span class="material-symbols-outlined text-[20px]">directions_car</span><span>Vehículos</span>
                </a>
                <a class="flex items-center gap-3 px-4 py-2.5 rounded-xl font-semibold text-slate-600 hover:bg-slate-100 hover:text-slate-900 transition-colors" href="#">
                    <span class="material-symbols-outlined text-[20px]">build</span><span>Mantenimiento</span>
                </a>
                <a class="flex items-center gap-3 px-4 py-2.5 rounded-xl font-semibold text-slate-600 hover:bg-slate-100 hover:text-slate-900 transition-colors" href="#">
                    <span class="material-symbols-outlined text-[20px]">badge</span><span>Conductores</span>
                </a>
                <a class="flex items-center gap-3 px-4 py-2.5 rounded-xl font-semibold text-slate-600 hover:bg-slate-100 hover:text-slate-900 transition-colors" href="#">
                    <span class="material-symbols-outlined text-[20px]">alt_route</span><span>Rutas Operativas</span>
                </a>
            </nav>
        </div>
        
        <!-- ZONA INFERIOR: CONFIGURACIÓN Y CERRAR SESIÓN -->
        <div class="flex flex-col gap-2 p-4 bg-slate-50 border-t border-slate-100">
            <a class="flex items-center gap-3 px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-200 hover:text-slate-900 transition-colors" href="#">
                <span class="material-symbols-outlined text-[20px]">settings</span><span>Configuración del Sistema</span>
            </a>
            <!-- Botón de Cerrar Sesión que destruye la sesión y redirige al login -->
            <a href="login.jsp?logout=true" onclick="<% session.invalidate(); %>" class="flex items-center justify-between w-full px-4 py-2.5 rounded-xl bg-red-100 text-red-700 hover:bg-red-200 transition-colors text-decoration-none">
                <span class="flex items-center gap-3 font-semibold text-sm">
                    <span class="material-symbols-outlined text-[20px]">logout</span><span>Cerrar Sesión</span>
                </span>
                <span class="material-symbols-outlined text-[16px]">arrow_forward</span>
            </a>
        </div>
    </aside>

    <!-- CONTENEDOR PRINCIPAL -->
    <div class="pl-72">
        <!-- BARRA SUPERIOR (NAVBAR) -->
        <header class="fixed top-0 left-72 right-0 h-16 bg-white/90 backdrop-blur-xl shadow-sm z-40 flex items-center justify-between px-6">
            <div class="flex items-center gap-4">
                <div class="flex items-center gap-2">
                    <span class="h-2.5 w-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span class="text-xs font-semibold text-slate-600">Nodo Activo (Cañete / Lima)</span>
                </div>
            </div>
            <div class="flex items-center gap-4">
                <div class="relative hidden md:block w-72">
                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[18px]">search</span>
                    <input class="w-full h-10 pl-9 pr-3 rounded-lg bg-slate-100 text-slate-900 placeholder:text-slate-400 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600" placeholder="Buscar vehículo, placa o chofer..." type="text"/>
                </div>
                <!-- Perfil del usuario conectado dinámicamente -->
                <div class="flex items-center gap-3 pl-2 border-l border-slate-200">
                    <div class="w-9 h-9 rounded-full bg-blue-600 text-white flex items-center justify-center font-bold text-sm">
                        <%= usuarioLogueado.substring(0, 2).toUpperCase() %>
                    </div>
                    <div class="hidden sm:flex flex-col text-left leading-tight">
                        <span class="text-sm font-bold text-slate-900"><%= usuarioLogueado %></span>
                        <span class="text-[11px] text-slate-500">Administrador de Flota</span>
                    </div>
                </div>
            </div>
        </header>

        <!-- CONTENIDO CENTRAL DEL DASHBOARD -->
        <main class="w-full pt-16 p-8">
            <div class="flex flex-col w-full gap-6">
                
                <!-- SECCIÓN DE BIENVENIDA -->
                <section class="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white p-6 rounded-xl shadow-sm border border-slate-100">
                    <div>
                        <span class="inline-flex items-center px-3 py-1 rounded-full bg-blue-50 text-blue-700 font-bold text-xs uppercase tracking-wide mb-2">
                            SISTEMA ACTIVO // MÓDULO MVC
                        </span>
                        <h1 class="text-2xl font-bold text-slate-900 tracking-tight">
                            Panel de Control - Gestión de Flota Vehicular
                        </h1>
                        <p class="text-sm text-slate-600 mt-1">
                            Supervisión en tiempo real del estado operativo, mantenimiento preventivo y control de accesos seguros.
                        </p>
                    </div>
                </section>

                <!-- TARJETAS DE MÉTRICAS (KPI GRID) -->
                <section class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
                    <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-100 flex flex-col justify-between">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Flota Operativa</span>
                            <div class="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center text-blue-600">
                                <span class="material-symbols-outlined text-[22px]">local_shipping</span>
                            </div>
                        </div>
                        <div class="mt-4">
                            <div class="flex items-baseline gap-2">
                                <span class="text-3xl font-extrabold text-slate-900">142</span>
                                <span class="text-sm font-semibold text-slate-400">/ 150</span>
                            </div>
                            <span class="inline-block mt-2 text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">94.6% Activa</span>
                        </div>
                    </div>
                    
                    <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-100 flex flex-col justify-between">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Unidades en Taller</span>
                            <div class="w-10 h-10 rounded-lg bg-amber-50 flex items-center justify-center text-amber-600">
                                <span class="material-symbols-outlined text-[22px]">build_circle</span>
                            </div>
                        </div>
                        <div class="mt-4">
                            <div class="flex items-baseline gap-2">
                                <span class="text-3xl font-extrabold text-slate-900">8</span>
                                <span class="text-sm font-semibold text-slate-400">Unidades</span>
                            </div>
                            <span class="inline-block mt-2 text-xs font-bold text-amber-700 bg-amber-50 px-2 py-0.5 rounded-full">3 Correctivos</span>
                        </div>
                    </div>

                    <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-100 flex flex-col justify-between">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Alertas & Revisiones</span>
                            <div class="w-10 h-10 rounded-lg bg-red-50 flex items-center justify-center text-red-600">
                                <span class="material-symbols-outlined text-[22px]">notification_important</span>
                            </div>
                        </div>
                        <div class="mt-4">
                            <div class="flex items-baseline gap-2">
                                <span class="text-3xl font-extrabold text-red-600">12</span>
                                <span class="text-sm font-semibold text-slate-400">Revisiones</span>
                            </div>
                            <span class="inline-block mt-2 text-xs font-bold text-red-700 bg-red-50 px-2 py-0.5 rounded-full">3 Críticas</span>
                        </div>
                    </div>

                    <div class="bg-white p-6 rounded-xl shadow-sm border border-slate-100 flex flex-col justify-between">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Rendimiento Ruta</span>
                            <div class="w-10 h-10 rounded-lg bg-emerald-50 flex items-center justify-center text-emerald-600">
                                <span class="material-symbols-outlined text-[22px]">speed</span>
                            </div>
                        </div>
                        <div class="mt-4">
                            <div class="flex items-baseline gap-2">
                                <span class="text-3xl font-extrabold text-slate-900">98.2%</span>
                                <span class="text-sm font-semibold text-slate-400">Eficiencia</span>
                            </div>
                            <span class="inline-block mt-2 text-xs font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">GPS Conectado</span>
                        </div>
                    </div>
                </section>

                <!-- TABLA DE FLOTA PRINCIPAL -->
                <section class="bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden">
                    <div class="p-6 border-b border-slate-100 flex items-center justify-between">
                        <h3 class="font-bold text-lg text-slate-900">Estado Reciente de Unidades</h3>
                        <span class="text-xs font-semibold text-slate-500">Actualizado vía OBD-II / Base de Datos PostgreSQL</span>
                    </div>
                    <div class="overflow-x-auto">
                        <table class="w-full text-left border-collapse">
                            <thead>
                                <tr class="bg-slate-50 text-slate-500 text-xs uppercase tracking-wider">
                                    <th class="p-4 font-bold">Placa</th>
                                    <th class="p-4 font-bold">Vehículo</th>
                                    <th class="p-4 font-bold">Conductor</th>
                                    <th class="p-4 font-bold">Kilometraje</th>
                                    <th class="p-4 font-bold">Estado Operativo</th>
                                    <th class="p-4 font-bold text-right">Acciones</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-slate-100 text-sm">
                                <tr class="hover:bg-slate-50 transition-colors">
                                    <td class="p-4 font-mono font-bold text-blue-600">FLT-4821</td>
                                    <td class="p-4 font-semibold text-slate-800">Volvo FH16 540</td>
                                    <td class="p-4 text-slate-600">Roberto Santos</td>
                                    <td class="p-4 text-slate-600 font-mono">142,350 km</td>
                                    <td class="p-4"><span class="px-3 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-700">En Ruta</span></td>
                                    <td class="p-4 text-right">
                                        <button class="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg font-semibold text-xs transition-colors">Detalles</button>
                                    </td>
                                </tr>
                                <tr class="hover:bg-slate-50 transition-colors">
                                    <td class="p-4 font-mono font-bold text-slate-600">FLT-9032</td>
                                    <td class="p-4 font-semibold text-slate-800">Mercedes-Benz Actros</td>
                                    <td class="p-4 text-slate-600">Miguel Ángel Vera</td>
                                    <td class="p-4 text-slate-600 font-mono">198,120 km</td>
                                    <td class="p-4"><span class="px-3 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700">En Taller</span></td>
                                    <td class="p-4 text-right">
                                        <button class="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg font-semibold text-xs transition-colors">Detalles</button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </section>

            </div>
        </main>
    </div>
</body>
</html>
