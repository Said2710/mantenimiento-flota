SET XACT_ABORT ON;
BEGIN TRANSACTION;

DECLARE @idUsuario int = (
    SELECT TOP (1) idUsuario FROM dbo.Usuarios WHERE estado = 'Activo' ORDER BY idUsuario
);
DECLARE @idCliente int = (
    SELECT TOP (1) idCliente FROM dbo.Cliente WHERE estado = 'Activo' ORDER BY idCliente
);
DECLARE @idVehiculo int = (
    SELECT TOP (1) idVehiculo FROM dbo.Vehiculo WHERE estado = 'Operativo' ORDER BY idVehiculo
);
DECLARE @idSolicitud int;

IF @idUsuario IS NULL
    THROW 51001, 'Se requiere un usuario activo para cargar la demostración.', 1;

IF NOT EXISTS (SELECT 1 FROM dbo.Cliente WHERE numeroDocumento = N'DEMO10-W10')
BEGIN
    INSERT INTO dbo.Cliente
        (razonSocial, numeroDocumento, estado, fechaCreacion, idUsuarioRegistro)
    VALUES
        (N'FleetPro - DEMO SEMANA 10', N'DEMO10-W10', N'Activo', SYSDATETIME(), @idUsuario);
END;

SET @idCliente = (SELECT TOP (1) idCliente FROM dbo.Cliente WHERE numeroDocumento = N'DEMO10-W10');
UPDATE dbo.Cliente SET estado = N'Activo' WHERE idCliente = @idCliente;

IF NOT EXISTS (SELECT 1 FROM dbo.Vehiculo WHERE placa = N'DEMO10')
BEGIN
    INSERT INTO dbo.Vehiculo
        (placa, marca, modelo, anio, estado, fechaCreacion, idUsuarioRegistro)
    VALUES
        (N'DEMO10', N'FleetPro', N'Unidad Demo W10', 2026, N'Operativo', SYSDATETIME(), @idUsuario);
END;

SET @idVehiculo = (SELECT TOP (1) idVehiculo FROM dbo.Vehiculo WHERE placa = N'DEMO10');
UPDATE dbo.Vehiculo SET estado = N'Operativo' WHERE idVehiculo = @idVehiculo;

IF NOT EXISTS (SELECT 1 FROM dbo.SolicitudServicio WHERE observacion = N'DEMO SEMANA 10')
BEGIN
    INSERT INTO dbo.SolicitudServicio (idCliente, estado, observacion, idUsuarioRegistro)
    VALUES (@idCliente, 'Pendiente', N'DEMO SEMANA 10', @idUsuario);

    SET @idSolicitud = CONVERT(int, SCOPE_IDENTITY());

    INSERT INTO dbo.DetalleSolicitudServicio
        (idSolicitud, idVehiculo, descripcionServicio, cantidad, precioEstimado)
    VALUES
        (@idSolicitud, @idVehiculo, N'Mantenimiento preventivo de demostración', 1, 250.00);
END;

COMMIT TRANSACTION;
