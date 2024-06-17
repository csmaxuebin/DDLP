function [knorm]=NormOfK(x,V)
%x is the point, V is the set of vertices; in isotropic space;
    V1=V(1,:);
	V2=V(2,:);
    n=size(V,2);
    a=0;
    knorm=0;

    for i=1:n-1
        j=i+1;
        b=(V1(j)-x(1)/x(2)*V2(j))/(x(1)/x(2)*(V2(i)-V2(j))-V1(i)+V1(j));

        if(b>=0 && b<=1)
            a=b/x(2)*(V2(i)-V2(j))+V2(j)/x(2);
        end

        if(a>=0)
            knorm=1/a;
        end        
    end
end

%test this funtion:
X=rand(10,1);
Y=rand(10,1);
V=convhull(X,Y);
W=[X(V)'-0.5;Y(V)'-0.5];
x=rand(2,1);
plot(W(1,:),W(2,:));
hold on;
plot(x(1),x(2),'o');
k=NormOfK(x,W);
plot(k*W(1,:),k*W(2,:),'r');

