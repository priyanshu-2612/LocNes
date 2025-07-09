package com.locnes;

public class Sprite {
    //these all are 1 byte each
    int y; // Y pos of the sprite
    int id; // Tile ID from pattern memory
    int attribute; // how the sprite should be rendered
    int x; // X pos of the sprite

    public Sprite getCopy(){
        Sprite sprite = new Sprite();
        sprite.y = y;
        sprite.id = id;
        sprite.attribute = attribute;
        sprite.x = x;
        return sprite;
    }
}
