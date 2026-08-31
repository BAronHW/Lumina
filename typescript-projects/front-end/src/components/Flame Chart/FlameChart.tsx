import React from 'react'

export interface FlameChartProps<T> {
    data: T[];
    getKey: (elem: T) => string;
    getParent: (elem: T) => T | null;
    getStart: (elem: T) => number;
    getDuration: (elem: T) => number;
    getLabel: (elem: T) => string;
    getStatus: (elem: T) => string;
}

export default function FlameChart() {
    /**
     * 1. x axis is duration and then y axis is the order
     * 2. write a function that would sort the data into order 
     * 3. some function that will determine where on the x and y axis the element will go depending on order and also duration of the element
     */

  return (
    <div>FlameChart</div>
  )
}
