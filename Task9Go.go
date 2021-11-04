package main

import (
	"fmt"
	"strconv"
	"sync"
	"time"
)

type Bargaining struct {
	channel chan struct{}
}

func (stop* Bargaining) lot() {
	stop.channel <- struct{}{}
}

func (stop* Bargaining) leave()  {
	<- stop.channel
}

func NewRequest(maxAmount int) *Bargaining {
	stop := &Bargaining{make(chan struct{}, maxAmount)}
	return stop
}

func lot(stops []*Bargaining, number int, wg* sync.WaitGroup) {
	for i := 0; i < len(stops); i++ {
		fmt.Println("Bargaining #" + strconv.Itoa(number) + " make a request #" + strconv.Ito(i))
		stops[i].lot()
		fmt.Println("Bargaining #" + strconv.Itoa(number) + " finish bet #" + strconv.Itoa(i))
		time.Sleep(time.Second)
		stops[i].leave()
		fmt.Println("Bargaining #" + strconv.Itoa(number) + " raise a request #" + strconv.Itoa(i))
	}
	wg.Done()
}

func main() {
	numOfLot := 5
	numOfStops := 3
	stops := make([]*Bargaining, numOfStops)
	for i := 0; i < numOfStops; i++ {
		stops[i] = NewRequest(i + 1)
	}

	wg := sync.WaitGroup{}
	wg.Add(numOfLot)
	for i := 0; i < numOfLot; i++ {
		go lot(stops, i, &wg)
	}
	wg.Wait()
}
