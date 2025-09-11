package com.playmatsec.app.repository.utils.Consts;

public class ResourceConsts {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String THUMBNAIL = "thumbnail";
    public static final String WATERMARK = "watermark";
    public static final String HOSTING = "hosting";
    public static final String TYPE = "type";

    // Campos anidados / relaciones
    public static final String RESOURCE_PRODUCTS = "resourceProducts";
    public static final String RESOURCE_PRODUCTS_IS_BANNER = "resourceProducts.isBanner";
    public static final String RESOURCE_PRODUCTS_PRODUCT_ID = "resourceProducts.product.id";
    public static final String CATEGORIES = "categories";

    // Rutas compuestas
    public static final String CATEGORIES_ID = CATEGORIES + ".id";
}
