package com.javarush.island.map;

import com.javarush.island.behavior.Eatable;
import com.javarush.island.behavior.Movable;
import com.javarush.island.behavior.Reproducible;
import com.javarush.island.entities.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Location implements Reproducible, Eatable, Movable {
    private int x;
    private int y;
    private volatile List<Entity> entityList = new ArrayList<>();

    @Override
    public void moveEntity() {

        GameMap gameMap = GameMap.getInstance();
        Iterator<Entity> entityIterator = entityList.iterator();

        while (entityIterator.hasNext()) {

            Entity nextEntity = entityIterator.next();
            Location newLocation = nextEntity.move(this);

            if (newLocation != null) {
                newLocation.entityList.add(nextEntity);
                entityIterator.remove();
            }
        }
    }

    @Override
    public void eatEntity() {

        Random random = new Random();

        for (var entity : entityList
        ) {
            if (entity.getMaxFood() > 0) {
                Map<Entity, Integer> victumMap = entity.getEatableTable();
                for (var victum : victumMap.entrySet()
                ) {
                    Optional<Entity> victumFilter = entityList.stream()
                            .filter(obj -> obj.equals(victum.getKey()) && obj.getMaxFood() > 0)
                            .findFirst();
                    //
                    if (victumFilter.isPresent() && random.nextInt(100) > (100 - victum.getValue()) ) {
                        victumFilter.get().setMaxFood(0);
                    } else {
                        entity.setMaxFood(0);
                    }
                }
            }
        }

        Iterator<Entity> entityIterator = entityList.iterator();

        while (entityIterator.hasNext()) {
            if (entityIterator.next().getMaxFood() == 0)
                entityIterator.remove();
        }

    }

    @Override
    public void reproduceEntity() {
        List<Entity> addList = new ArrayList<>(entityList.size() / 4); // Предварительное выделение с оценочным размером

        Random random = new Random();

        // Сортировка entityList
        entityList.sort(Comparator.comparingInt(Entity::hashCode));

        for (int i = 1; i < entityList.size(); i += 2) {
            Entity entity1 = entityList.get(i - 1);
            Entity entity2 = entityList.get(i);

            if (entity1.equals(entity2) && random.nextInt(100) > 80) {
                Entity newEntity = entity2.createNewEntity();
                long count = entityList.stream()
                        .filter(entity -> entity.equals(newEntity))
                        .count();

                if (count < newEntity.getMaxAmount()) { // Порверяем, что нет превышения по кол-ву в ячейке по каждому виду
                    addList.add(newEntity);
                }
            }
        }
        entityList.addAll(addList); // Добавление всех новых сущностей в entityList
    }

}
