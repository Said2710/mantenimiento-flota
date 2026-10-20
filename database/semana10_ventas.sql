SET XACT_ABORT ON;
BEGIN TRY
BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.Cliente', N'U') IS NULL OR OBJECT_ID(N'dbo.Vehiculo', N'U') IS NULL
    THROW 51000, 'Se requieren las tablas dbo.Cliente y dbo.Vehiculo.', 1;

IF OBJECT_ID(N'dbo.Usuarios', N'U') IS NULL OR OBJECT_ID(N'dbo.VentaServicio', N'U') IS NULL
    THROW 51000, 'Se requieren las tablas dbo.Usuarios y dbo.VentaServicio.', 1;

IF OBJECT_ID(N'dbo.DetalleVentaServicio', N'U') IS NULL
    THROW 51000, 'Se requiere la tabla dbo.DetalleVentaServicio.', 1;

IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID(N'dbo.VentaServicio')
      AND name = N'CK_VentaServicio_estado'
)
    ALTER TABLE dbo.VentaServicio DROP CONSTRAINT CK_VentaServicio_estado;

ALTER TABLE dbo.VentaServicio
    ADD CONSTRAINT CK_VentaServicio_estado
    CHECK (estado IN (N'Pendiente', N'Confirmada', N'Facturada', N'Anulada', N'En curso', N'Completada'));

IF OBJECT_ID(N'dbo.SolicitudServicio', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SolicitudServicio (
        idSolicitud int IDENTITY(1,1) NOT NULL CONSTRAINT PK_SolicitudServicio PRIMARY KEY,
        idCliente int NOT NULL,
        fechaSolicitud datetime2(0) NOT NULL CONSTRAINT DF_SolicitudServicio_fecha DEFAULT SYSDATETIME(),
        estado varchar(15) NOT NULL CONSTRAINT DF_SolicitudServicio_estado DEFAULT 'Pendiente',
        observacion nvarchar(300) NULL,
        idUsuarioRegistro int NOT NULL,
        idVenta int NULL,
        CONSTRAINT CK_SolicitudServicio_estado CHECK (estado IN ('Pendiente', 'Aprobada', 'Convertida', 'Anulada')),
        CONSTRAINT FK_SolicitudServicio_Cliente FOREIGN KEY (idCliente) REFERENCES dbo.Cliente(idCliente),
        CONSTRAINT FK_SolicitudServicio_Usuario FOREIGN KEY (idUsuarioRegistro) REFERENCES dbo.Usuarios(idUsuario),
        CONSTRAINT FK_SolicitudServicio_Venta FOREIGN KEY (idVenta) REFERENCES dbo.VentaServicio(idVenta)
    );
END;

IF OBJECT_ID(N'dbo.DetalleSolicitudServicio', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DetalleSolicitudServicio (
        idDetalleSolicitud int IDENTITY(1,1) NOT NULL CONSTRAINT PK_DetalleSolicitudServicio PRIMARY KEY,
        idSolicitud int NOT NULL,
        idVehiculo int NOT NULL,
        descripcionServicio nvarchar(200) NOT NULL,
        origen nvarchar(150) NULL,
        destino nvarchar(150) NULL,
        fechaServicio datetime2(0) NULL,
        cantidad decimal(9,2) NOT NULL CONSTRAINT DF_DetalleSolicitudServicio_cantidad DEFAULT (1),
        precioEstimado decimal(9,2) NOT NULL,
        CONSTRAINT CK_DetalleSolicitudServicio_cantidad CHECK (cantidad > 0),
        CONSTRAINT CK_DetalleSolicitudServicio_precio CHECK (precioEstimado >= 0),
        CONSTRAINT FK_DetalleSolicitudServicio_Solicitud FOREIGN KEY (idSolicitud) REFERENCES dbo.SolicitudServicio(idSolicitud),
        CONSTRAINT FK_DetalleSolicitudServicio_Vehiculo FOREIGN KEY (idVehiculo) REFERENCES dbo.Vehiculo(idVehiculo)
    );
END;

IF OBJECT_ID(N'dbo.DocumentoVenta', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DocumentoVenta (
        idDocumento int IDENTITY(1,1) NOT NULL CONSTRAINT PK_DocumentoVenta PRIMARY KEY,
        idVenta int NOT NULL,
        tipoDocumento varchar(10) NOT NULL,
        serie varchar(10) NOT NULL,
        numero varchar(20) NOT NULL,
        fechaEmision datetime2(0) NOT NULL CONSTRAINT DF_DocumentoVenta_fecha DEFAULT SYSDATETIME(),
        estado varchar(15) NOT NULL CONSTRAINT DF_DocumentoVenta_estado DEFAULT 'Emitido',
        observacion nvarchar(300) NULL,
        idUsuarioRegistro int NOT NULL,
        CONSTRAINT CK_DocumentoVenta_tipo CHECK (tipoDocumento IN ('Factura', 'Boleta', 'Guia')),
        CONSTRAINT CK_DocumentoVenta_estado CHECK (estado IN ('Emitido', 'Anulado')),
        CONSTRAINT FK_DocumentoVenta_Venta FOREIGN KEY (idVenta) REFERENCES dbo.VentaServicio(idVenta),
        CONSTRAINT FK_DocumentoVenta_Usuario FOREIGN KEY (idUsuarioRegistro) REFERENCES dbo.Usuarios(idUsuario)
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.SolicitudServicio') AND name = N'UX_SolicitudServicio_Venta')
    CREATE UNIQUE INDEX UX_SolicitudServicio_Venta ON dbo.SolicitudServicio(idVenta) WHERE idVenta IS NOT NULL;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.SolicitudServicio') AND name = N'IX_SolicitudServicio_estado_fecha')
    CREATE INDEX IX_SolicitudServicio_estado_fecha ON dbo.SolicitudServicio(estado, fechaSolicitud DESC);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.DetalleSolicitudServicio') AND name = N'IX_DetalleSolicitudServicio_solicitud')
    CREATE INDEX IX_DetalleSolicitudServicio_solicitud ON dbo.DetalleSolicitudServicio(idSolicitud);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.DocumentoVenta') AND name = N'UX_DocumentoVenta_numero')
    CREATE UNIQUE INDEX UX_DocumentoVenta_numero ON dbo.DocumentoVenta(tipoDocumento, serie, numero);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.DocumentoVenta') AND name = N'IX_DocumentoVenta_venta')
    CREATE INDEX IX_DocumentoVenta_venta ON dbo.DocumentoVenta(idVenta, estado);

COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
