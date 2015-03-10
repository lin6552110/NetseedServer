package com.lin.test;

public class ThreadTest {
	public static void main(String[] args) {
		Cash cash=new Cash();
		cash.setNum(0);

		
		ThreadLin lin=new ThreadLin(cash);
		ThreadLin2 lin2=new ThreadLin2(cash);		
		lin2.start();
		lin.start();
		
	}
	
	
	

}

class Cash{
	private int num;
	private int m;
	private char c;
	
	
	public int getM() {
		return m;
	}

	public void setM(int m) {
		this.m = m;
	}

	public char getC() {
		return c;
	}

	public void setC(char c) {
		this.c = c;
	}

	public int getNum(){
		return this.num;
	}
	
	public void setNum(int num){
		this.num=num;
	}
	
	public synchronized  void deposit(int n){
		for(int i=0;i<n;i++){
			notifyAll();
			this.num++;
			System.out.println("B:"+getNum()+"    "+i);
			try {
				Thread.sleep(300);
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	
	public synchronized void getMoney(int n){
		for(int i=0;i<n;i++){
			notifyAll();
			this.num--;
			System.out.println("A:"+getNum()+"    "+i);
			try {
				this.wait();
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void doit(){
		switch (c) {
		case '-':
			getMoney(m);
			break;
		case '+':
			deposit(m);
			break;
		}
		
	}
	
}

class ThreadLin extends Thread{
	
	private Cash cash;
	
	public ThreadLin(Cash cash){
		this.cash=cash;
	}
	
	@Override
	public void run() {
		cash.getMoney(100);
	}

}

class ThreadLin2 extends Thread{
	
	private Cash cash;
	
	public ThreadLin2(Cash cash){
		this.cash=cash;
	}
	
	@Override
	public void run() {
		cash.deposit(100);
	}
	
}